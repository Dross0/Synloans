package com.synloans.loans.service.loan;

import com.synloans.loans.model.dto.collection.LoanRequestCollection;
import com.synloans.loans.model.dto.loanrequest.LoanRequestDto;
import com.synloans.loans.model.dto.loanrequest.LoanRequestStatus;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.loan.Loan;
import com.synloans.loans.model.entity.loan.LoanRequest;
import com.synloans.loans.model.entity.syndicate.Syndicate;
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant;
import com.synloans.loans.repository.loan.LoanRequestRepository;
import com.synloans.loans.service.exception.ForbiddenResourceException;
import com.synloans.loans.service.exception.InvalidLoanRequestException;
import com.synloans.loans.service.exception.notfound.LoanRequestNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanRequestService {
    private final LoanRequestRepository loanRequestRepository;

    public LoanRequest createRequest(LoanRequestDto loanRequestDto, Company company) {
        LoanRequest loanRequest = new LoanRequest();
        loanRequest.setCompany(company);
        loanRequest.setTerm(loanRequestDto.getTerm());
        loanRequest.setRate(loanRequestDto.getMaxRate());
        loanRequest.setSum(loanRequestDto.getSum());
        loanRequest.setCreateDate(LocalDate.now());
        return loanRequestRepository.save(loanRequest);
    }

    public LoanRequest save(LoanRequest loanRequest){
        return loanRequestRepository.save(loanRequest);
    }

    public LoanRequestCollection getAll(Company company) {
        LoanRequestCollection loanRequestCollection = new LoanRequestCollection();
        Collection<LoanRequest> allRequest = loanRequestRepository.findAll();
        for (LoanRequest loanRequest: allRequest){
            if (loanRequest.getSyndicate() != null){
                boolean isOwn = loanRequest.getSyndicate().getParticipants()
                        .stream()
                        .anyMatch(syndicateParticipant ->
                                Objects.equals(syndicateParticipant.getBank().getCompany(), company)
                        );
                if (isOwn){
                    loanRequestCollection.addOwn(loanRequest);
                } else {
                    loanRequestCollection.addOther(loanRequest);
                }
            }
            else {
                loanRequestCollection.addOther(loanRequest);
            }
        }
        return loanRequestCollection;
    }

    public void deleteById(Long id) {
        loanRequestRepository.deleteById(id);
    }

    public Optional<LoanRequest> getById(Long id){
        return loanRequestRepository.findById(id);
    }

    public LoanRequest getOwnedCompanyLoanRequestById(long id, Company company){
        LoanRequest loanRequest = loanRequestRepository
                .findById(id)
                .orElseThrow(LoanRequestNotFoundException::new);
        if (!company.getId().equals(loanRequest.getCompany().getId())){
            log.error("Заявка не принадлежит компании");
            throw new ForbiddenResourceException("Заявка с id=" + id + " не принадлежит компании=" + company.getShortName());
        }
        return loanRequest;
    }

    public LoanRequestStatus getStatus(LoanRequest loanRequest) {
        Loan loan = loanRequest.getLoan();
        if (loan == null){
            if (isReadyToIssue(loanRequest)){
                return LoanRequestStatus.READY_TO_ISSUE;
            }
            return LoanRequestStatus.OPEN;
        }
        if (LocalDate.now().isAfter(loan.getCloseDate())){
            return LoanRequestStatus.CLOSE;
        }
        return LoanRequestStatus.ISSUE;
    }

    private boolean isReadyToIssue(LoanRequest loanRequest){
        try {
            return calcSumFromSyndicate(loanRequest) >= loanRequest.getSum();
        } catch (InvalidLoanRequestException e){
            return false;
        }
    }


    private long calcSumFromSyndicate(LoanRequest loanRequest){
        Syndicate syndicate = loanRequest.getSyndicate();
        if (syndicate == null){
            throw new InvalidLoanRequestException("Синдиката на заявке нет");
        }
        return syndicate.getParticipants().stream()
                .mapToLong(SyndicateParticipant::getLoanSum)
                .sum();
    }
}
