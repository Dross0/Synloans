package com.synloans.loans.model.mapper;

import com.synloans.loans.model.dto.BankParticipantInfo;
import com.synloans.loans.model.dto.LoanSum;
import com.synloans.loans.model.entity.SyndicateParticipant;

public class SyndicateParticipantMapper {
    public BankParticipantInfo entityToDto(SyndicateParticipant participant){
        return BankParticipantInfo.builder()
                .id(participant.getBank().getId())
                .name(participant.getBank().getCompany().getShortName())
                .sum(LoanSum.valueOf(participant.getLoanSum()))
                .approveBankAgent(participant.isApproveBankAgent())
                .build();
    }
}