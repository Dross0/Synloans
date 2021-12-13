package com.synloans.loans.service.loan;

import com.synloans.loans.model.entity.Bank;
import com.synloans.loans.model.entity.Loan;
import com.synloans.loans.model.entity.LoanRequest;
import com.synloans.loans.model.entity.SyndicateParticipant;
import com.synloans.loans.repository.loan.LoanRepository;
import com.synloans.loans.service.exception.InvalidLoanRequestException;
import com.synloans.loans.service.syndicate.SyndicateParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loanRepository;
    private final LoanRequestService loanRequestService;
    private final SyndicateParticipantService participantService;

    @Transactional
    public Loan startLoan(LoanRequest loanRequest){
        validateLoanRequest(loanRequest);
        List<SyndicateParticipant> finalSyndicateParticipants = buildFinalParticipants(loanRequest);
        Bank bankAgent = findBankAgent(finalSyndicateParticipants);
        return save(buildLoan(loanRequest, bankAgent));
    }

    private Loan buildLoan(LoanRequest loanRequest, Bank bankAgent) {
        LocalDate registrationDate = LocalDate.now();
        Loan loan = new Loan();
        loan.setBankAgent(bankAgent);
        loan.setRequest(loanRequest);
        loan.setRegistrationDate(registrationDate);
        loan.setRate(loanRequest.getRate());
        loan.setCloseDate(registrationDate.plusMonths(loanRequest.getTerm()));
        loan.setSum((double) loanRequest.getSum());
        return loan;
    }

    public Loan save(Loan loan){
        return loanRepository.save(loan);
    }

    private Bank findBankAgent(List<SyndicateParticipant> finalSyndicateParticipants) {
        return finalSyndicateParticipants.stream()
                .filter(SyndicateParticipant::isApproveBankAgent)
                .findFirst()
                .orElse(finalSyndicateParticipants.get(0))
                .getBank();

    }

    private List<SyndicateParticipant> buildFinalParticipants(LoanRequest loanRequest) {
        List<SyndicateParticipant> allSortedParticipants = loanRequest.getSyndicate()
                .getParticipants()
                .stream()
                .sorted(Comparator.comparingLong(SyndicateParticipant::getLoanSum).reversed())
                .collect(Collectors.toList());
        List<SyndicateParticipant> result = new ArrayList<>();
        long sum = 0;
        for (SyndicateParticipant participant: allSortedParticipants){
            if (sum < loanRequest.getSum()) {
                long issueSum = participant.getLoanSum();
                if (sum + participant.getLoanSum() > loanRequest.getSum()) {
                    issueSum = loanRequest.getSum() - sum;
                }
                participant.setIssuedLoanSum(issueSum);
                result.add(participant);
                sum += participant.getLoanSum();
            } else {
                participant.setIssuedLoanSum(null);
            }
        }
        participantService.saveAll(allSortedParticipants);
        return result;
    }

    private void validateLoanRequest(LoanRequest loanRequest) {
        if (loanRequest.getLoan() != null){
            throw new InvalidLoanRequestException("Кредит уже существует");
        }
        long sumFromSyndicate = loanRequestService.calcSumFromSyndicate(loanRequest);
        if (sumFromSyndicate < loanRequest.getSum()){
            throw new InvalidLoanRequestException("Собранной суммы недостаточно для выдачи кредита");
        }
    }
}
