package com.sinloans.loans.service;

import com.sinloans.loans.model.entity.Company;
import com.sinloans.loans.model.entity.LoanRequest;
import com.sinloans.loans.model.dto.LoanRequestDto;
import com.sinloans.loans.repositories.LoanRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;

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
}
