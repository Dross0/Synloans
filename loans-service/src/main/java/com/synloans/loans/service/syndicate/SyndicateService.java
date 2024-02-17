package com.synloans.loans.service.syndicate;

import com.synloans.loans.model.dto.SyndicateJoinRequest;
import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.model.entity.syndicate.Syndicate;
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant;

import java.util.Optional;

public interface SyndicateService {
    Optional<Syndicate> getByLoanRequestId(long loanRequestId);

    Optional<SyndicateParticipant> joinBankToSyndicate(SyndicateJoinRequest joinRequest, Bank bank);
}
