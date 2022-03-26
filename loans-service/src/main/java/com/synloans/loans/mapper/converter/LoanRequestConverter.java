package com.synloans.loans.mapper.converter;

import com.synloans.loans.model.dto.LoanSum;
import com.synloans.loans.model.dto.loanrequest.LoanRequestInfo;
import com.synloans.loans.model.entity.loan.LoanRequest;
import com.synloans.loans.service.loan.LoanRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class LoanRequestConverter implements Converter<LoanRequest, LoanRequestInfo> {

    private final LoanRequestService loanRequestService;

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
                .status(loanRequestService.getStatus(loanRequest))
                .build();
    }
}
