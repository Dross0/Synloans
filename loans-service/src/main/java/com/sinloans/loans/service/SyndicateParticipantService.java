package com.sinloans.loans.service;

import com.sinloans.loans.model.entity.Bank;
import com.sinloans.loans.model.entity.Syndicate;
import com.sinloans.loans.model.entity.SyndicateParticipant;
import com.sinloans.loans.repositories.SyndicateParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SyndicateParticipantService {
    private final SyndicateParticipantRepository participantRepository;

    public SyndicateParticipant createNewParticipant(Syndicate syndicate, Bank bank, long loanSum, boolean approveBankAgent){
        SyndicateParticipant participant = new SyndicateParticipant();
        participant.setApproveBankAgent(approveBankAgent);
        participant.setSyndicate(syndicate);
        participant.setBank(bank);
        participant.setLoanSum(loanSum);
        return participantRepository.save(participant);
    }

    public void quitFromSyndicate(Long loanRequestId, Bank bank) {
        for (SyndicateParticipant syndicateParticipant: bank.getSyndicates()){
            if (Objects.equals(syndicateParticipant.getSyndicate().getRequest().getId(), loanRequestId)){
                participantRepository.delete(syndicateParticipant);
                return;
            }
        }
    }
}
