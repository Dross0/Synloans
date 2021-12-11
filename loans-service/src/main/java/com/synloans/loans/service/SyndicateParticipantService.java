package com.synloans.loans.service;

import com.synloans.loans.model.entity.Bank;
import com.synloans.loans.model.entity.Syndicate;
import com.synloans.loans.model.entity.SyndicateParticipant;
import com.synloans.loans.repositories.SyndicateParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SyndicateParticipantService {
    private final SyndicateParticipantRepository participantRepository;

    @Transactional
    public SyndicateParticipant createNewParticipant(Syndicate syndicate, Bank bank, long loanSum, boolean approveBankAgent){
        SyndicateParticipant participant = new SyndicateParticipant();
        participant.setApproveBankAgent(approveBankAgent);
        participant.setSyndicate(syndicate);
        participant.setBank(bank);
        participant.setLoanSum(loanSum);
        return participantRepository.save(participant);
    }

    @Transactional
    public void quitFromSyndicate(Long loanRequestId, Bank bank) {
        for (SyndicateParticipant syndicateParticipant: bank.getSyndicates()){
            if (Objects.equals(syndicateParticipant.getSyndicate().getRequest().getId(), loanRequestId)){
                participantRepository.delete(syndicateParticipant);
                return;
            }
        }
    }
}
