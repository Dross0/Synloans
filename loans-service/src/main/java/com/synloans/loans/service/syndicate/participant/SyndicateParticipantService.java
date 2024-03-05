package com.synloans.loans.service.syndicate.participant;

import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.model.entity.syndicate.Syndicate;
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant;

import java.util.Collection;

public interface SyndicateParticipantService {
    SyndicateParticipant createNewParticipant(Syndicate syndicate, Bank bank, long loanSum, boolean approveBankAgent);

    Collection<SyndicateParticipant> saveAll(Collection<SyndicateParticipant> syndicateParticipants);

    void quitFromSyndicate(Long loanRequestId, Bank bank);

    Collection<SyndicateParticipant> getSyndicateParticipantsByRequestId(long id);
}
