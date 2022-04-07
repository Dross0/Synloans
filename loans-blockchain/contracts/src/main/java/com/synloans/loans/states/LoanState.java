package com.synloans.loans.states;

import com.synloans.loans.contracts.LoanContract;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
@BelongsToContract(LoanContract.class)
public class LoanState implements LinearState {
    private final UniqueIdentifier id;

    private final Party borrower;

    private final long loanSum;

    private final int term;

    private final double rate;

    private final List<Party> banks;

    private final Party agentBank;


    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        List<AbstractParty> parties = new ArrayList<>(banks);
        parties.add(borrower);
        return parties;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return id;
    }
}
