package com.synloans.loans.service.loan;

import com.synloans.loans.AnnuityLoan;
import com.synloans.loans.factory.AnnuityLoanFactory;
import com.synloans.loans.info.LoanInfo;
import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.model.entity.loan.Loan;
import com.synloans.loans.model.entity.loan.LoanRequest;
import com.synloans.loans.model.entity.loan.payment.PlannedPayment;
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant;
import com.synloans.loans.payment.LoanPayment;
import com.synloans.loans.repository.loan.LoanRepository;
import com.synloans.loans.service.exception.InvalidLoanRequestException;
import com.synloans.loans.service.loan.payment.PlannedPaymentService;
import com.synloans.loans.service.syndicate.SyndicateParticipantService;
import lombok.RequiredArgsConstructor;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {
    private static final String CURRENCY_CODE = "RUR";

    private final LoanRepository loanRepository;
    private final LoanRequestService loanRequestService;
    private final SyndicateParticipantService participantService;
    private final PlannedPaymentService plannedPaymentService;

    @Transactional
    public Loan startLoan(LoanRequest loanRequest){
        validateLoanRequest(loanRequest);
        List<SyndicateParticipant> finalSyndicateParticipants = buildFinalParticipants(loanRequest);
        Bank bankAgent = findBankAgent(finalSyndicateParticipants);
        Loan loan = save(buildLoan(loanRequest, bankAgent));
        plannedPaymentService.save(buildPlannedPayments(loan));
        return loan;
    }

    private List<PlannedPayment> buildPlannedPayments(Loan loan) {
        BigDecimal rate = BigDecimal.valueOf(loan.getRate() / 100);
        Money loanSum = Money.of(loan.getSum(), CURRENCY_CODE);
        LoanInfo loanInfo = new LoanInfo(
                loanSum,
                rate,
                loan.getRegistrationDate(),
                loan.getRequest().getTerm()
        );
        AnnuityLoan annuityLoan = new AnnuityLoanFactory().create(loanInfo);
        return annuityLoan.getPaymentsList().stream()
                .map(this::toPlannedPayment)
                .peek(plannedPayment -> plannedPayment.setLoan(loan))
                .collect(Collectors.toList());
    }

    private PlannedPayment toPlannedPayment(LoanPayment loanPayment) {
        PlannedPayment plannedPayment = new PlannedPayment();
        plannedPayment.setPrincipal(
                loanPayment.getPaymentSum()
                        .getPrincipalPart()
                        .getNumberStripped()
        );
        plannedPayment.setPercent(
                loanPayment.getPaymentSum()
                        .getPercentPart()
                        .getNumberStripped()
        );
        plannedPayment.setDate(loanPayment.getDate());
        return plannedPayment;
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
