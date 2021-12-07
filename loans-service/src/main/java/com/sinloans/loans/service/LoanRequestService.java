package com.sinloans.loans.service;

import com.sinloans.loans.model.dto.loanrequest.LoanRequestDto;
import com.sinloans.loans.model.dto.loanrequest.LoanRequestStatus;
import com.sinloans.loans.model.entity.Company;
import com.sinloans.loans.model.entity.Loan;
import com.sinloans.loans.model.entity.LoanRequest;
import com.sinloans.loans.repositories.LoanRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
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
}
