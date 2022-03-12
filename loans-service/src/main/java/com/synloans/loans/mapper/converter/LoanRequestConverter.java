package com.synloans.loans.mapper.converter;

import com.synloans.loans.model.dto.LoanSum;
import com.synloans.loans.model.dto.loanrequest.LoanRequestInfo;
import com.synloans.loans.model.entity.loan.LoanRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class LoanRequestConverter implements Converter<LoanRequest, LoanRequestInfo> {

    @Override
    public LoanRequestInfo convert(LoanRequest loanRequest){
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
