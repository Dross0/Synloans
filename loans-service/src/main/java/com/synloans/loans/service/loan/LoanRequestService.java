package com.synloans.loans.service.loan;

import com.synloans.loans.model.dto.loanrequest.LoanRequestDto;
import com.synloans.loans.model.dto.loanrequest.LoanRequestStatus;
import com.synloans.loans.model.entity.*;
import com.synloans.loans.repository.loan.LoanRequestRepository;
import com.synloans.loans.service.exception.ForbiddenResourceException;
import com.synloans.loans.service.exception.InvalidLoanRequestException;
import com.synloans.loans.service.exception.LoanRequestNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
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
        loanRequest.setSum(loanRequestDto.getSum().getSum());
        loanRequest.setCreateDate(LocalDate.now());
        return loanRequestRepository.save(loanRequest);
    }

    public LoanRequest save(LoanRequest loanRequest){
        return loanRequestRepository.save(loanRequest);
    }

    public Collection<LoanRequest> getAll() {
        return loanRequestRepository.findAll();
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
            return LoanRequestStatus.OPEN;
        }
        if (LocalDate.now().isAfter(loan.getCloseDate())){
            return LoanRequestStatus.CLOSE;
        }
        return LoanRequestStatus.ISSUE;  //FIXME Подумать как обрабатывать статус transfer
    }

    public long calcSumFromSyndicate(LoanRequest loanRequest){
        Syndicate syndicate = loanRequest.getSyndicate();
        if (syndicate == null){
            throw new InvalidLoanRequestException("Синдиката на заявке нет");
        }
        return syndicate.getParticipants().stream()
                .mapToLong(SyndicateParticipant::getLoanSum)
                .sum();
    }
}
