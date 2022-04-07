package com.synloans.loans.states;

import com.synloans.loans.contracts.SyndicateParticipantContract;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.StaticPointer;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Getter
@BelongsToContract(SyndicateParticipantContract.class)
public class SyndicateParticipantState implements LinearState {

    private final UniqueIdentifier id;

    private final StaticPointer<LoanState> loanState;

    private final Party bank;

    private final long issuedLoanSum;

    private final Party borrower;

    private final Party agentBank;

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        Set<AbstractParty> parties = new HashSet<>();
        parties.add(borrower);
        parties.add(bank);
        parties.add(agentBank);
        return new ArrayList<>(parties);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return id;
    }
}
