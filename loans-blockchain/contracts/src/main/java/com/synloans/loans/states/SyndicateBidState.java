package com.synloans.loans.states;

import com.synloans.loans.contracts.SyndicateBidContract;
import com.synloans.loans.status.SyndicateBidStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearPointer;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(SyndicateBidContract.class)
@RequiredArgsConstructor
@Getter
public class SyndicateBidState implements LinearState {

    private final UniqueIdentifier uniqueIdentifier;

    private final LinearPointer<SyndicateState> syndicateState;

    private final long bidAmount;

    private final Party leadBank;

    private final Party participantBank;

    private final SyndicateBidStatus status;


    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(participantBank, leadBank);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return uniqueIdentifier;
    }
}
