package com.synloans.loans.states;

import com.synloans.loans.contracts.SyndicateContract;
import lombok.Getter;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearPointer;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@BelongsToContract(SyndicateContract.class)
@Getter
public class SyndicateState implements LinearState {

    private final UniqueIdentifier uniqueIdentifier;

    private final Party leadBank;

    private final List<Party> participantBanks;

    private final LinearPointer<ProjectState> projectDetails;

    private final LinearPointer<LoanBidState> loanDetails;


    public SyndicateState(
            UniqueIdentifier uniqueIdentifier,
            Party leadBank,
            List<Party> participantBanks,
            LinearPointer<ProjectState> projectDetails,
            LinearPointer<LoanBidState> loanDetails
    ) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.leadBank = leadBank;
        this.participantBanks = new ArrayList<>(participantBanks);
        this.projectDetails = projectDetails;
        this.loanDetails = loanDetails;
    }

    public Party getLeadBank() {
        return leadBank;
    }

    public List<Party> getParticipantBanks() {
        return Collections.unmodifiableList(participantBanks);
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        List<AbstractParty> participants = new ArrayList<>(participantBanks);
        participants.add(leadBank);
        return participants;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return uniqueIdentifier;
    }
}
