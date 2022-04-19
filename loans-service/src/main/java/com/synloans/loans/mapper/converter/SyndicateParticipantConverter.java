package com.synloans.loans.mapper.converter;

import com.synloans.loans.model.dto.BankParticipantInfo;
import com.synloans.loans.model.dto.LoanSum;
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SyndicateParticipantConverter implements Converter<SyndicateParticipant, BankParticipantInfo> {

    @Override
    public BankParticipantInfo convert(SyndicateParticipant participant){
        long issuedLoanSum = 0;
        if (participant.getIssuedLoanSum() != null){
            issuedLoanSum = participant.getIssuedLoanSum();
        }
        return BankParticipantInfo.builder()
                .id(participant.getBank().getId())
                .name(participant.getBank().getCompany().getFullName())
                .sum(LoanSum.valueOf(participant.getLoanSum()))
                .issuedSum(issuedLoanSum)
                .approveBankAgent(participant.isApproveBankAgent())
                .build();
    }
}
