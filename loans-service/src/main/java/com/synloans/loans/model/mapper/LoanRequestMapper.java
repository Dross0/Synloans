package com.synloans.loans.model.mapper;

import com.synloans.loans.model.dto.LoanSum;
import com.synloans.loans.model.dto.loanrequest.LoanRequestInfo;
import com.synloans.loans.model.entity.loan.LoanRequest;

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
