package com.synloans.loans.mapper.converter;

import com.synloans.loans.mapper.Mapper;
import com.synloans.loans.model.dto.BankParticipantInfo;
import com.synloans.loans.model.dto.CompanyDto;
import com.synloans.loans.model.dto.LoanSum;
import com.synloans.loans.model.dto.loanrequest.LoanRequestInfo;
import com.synloans.loans.model.dto.loanrequest.LoanRequestResponse;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.loan.LoanRequest;
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant;
import com.synloans.loans.service.loan.LoanRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LoanRequestConverter implements Converter<LoanRequest, LoanRequestResponse> {

    private final LoanRequestService loanRequestService;

    private final Converter<SyndicateParticipant, BankParticipantInfo> syndicateParticipantConverter;

    private final Mapper<Company, CompanyDto> companyMapper;

    @Override
    public LoanRequestResponse convert(LoanRequest loanRequest){
        LoanRequestResponse response = new LoanRequestResponse();
        response.setInfo(buildRequestInfo(loanRequest));
        response.setBanks(Collections.emptyList());
        if (loanRequest.getSyndicate() != null){
            response.setBanks(
                    loanRequest.getSyndicate()
                            .getParticipants()
                            .stream()
                            .map(syndicateParticipantConverter::convert)
                            .collect(Collectors.toList())
            );
        }
        response.setBorrower(
                companyMapper.mapFrom(loanRequest.getCompany())
        );

        return response;


    }

    private LoanRequestInfo buildRequestInfo(LoanRequest loanRequest){
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
