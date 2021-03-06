package com.synloans.loans.repository.syndicate;

import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyndicateParticipantRepository extends JpaRepository<SyndicateParticipant, Long> {
    void deleteByBankAndSyndicate_Request_Id(Bank bank, long loanRequestId);
}
