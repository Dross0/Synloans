package com.sinloans.loans.model.mapper;

import com.sinloans.loans.model.dto.LoanSum;
import com.sinloans.loans.model.dto.loanrequest.LoanRequestInfo;
import com.sinloans.loans.model.entity.LoanRequest;

import java.time.LocalDate;

public class LoanRequestMapper {
    public LoanRequestInfo entityToDto(LoanRequest loanRequest){
        LocalDate issueDate = null;
        if (loanRequest.getLoan() != null){
            issueDate = loanRequest.getLoan().getRegistrationDate();
        }
        return LoanRequestInfo.builder()
                .id(loanRequest.getId())
                .term(loanRequest.getTerm())
                .dateCreate(loanRequest.getCreateDate())
                .maxRate(loanRequest.getRate())
                .dateIssue(issueDate)
                .sum(LoanSum.valueOf(loanRequest.getSum()))
                .build();
    }
}
