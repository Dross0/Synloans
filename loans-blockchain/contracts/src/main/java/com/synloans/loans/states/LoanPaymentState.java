package com.synloans.loans.states;

import com.synloans.loans.contracts.LoanPaymentContract;
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
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Getter
@BelongsToContract(LoanPaymentContract.class)
public class LoanPaymentState implements LinearState {

    private final UniqueIdentifier id;

    private final StaticPointer<LoanState> loanState;

    private final Party borrower;

    private final long payment;

    private final Collection<Party> banks;

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
