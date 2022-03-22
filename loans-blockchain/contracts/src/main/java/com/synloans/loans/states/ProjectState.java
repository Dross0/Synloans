package com.synloans.loans.states;

import com.synloans.loans.contracts.ProjectContract;
import lombok.Getter;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@BelongsToContract(ProjectContract.class)
@Getter
public class ProjectState implements LinearState {

    private final UniqueIdentifier uniqueIdentifier;

    private final String projectDescription;

    private final Party borrower;

    private final long loanAmount;

    private final List<Party> lenders;


    public ProjectState(
            UniqueIdentifier uniqueIdentifier,
            String projectDescription,
            Party borrower,
            long loanAmount,
            List<Party> lenders
    ) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.projectDescription = projectDescription;
        this.borrower = borrower;
        this.loanAmount = loanAmount;
        this.lenders = new ArrayList<>(lenders);
    }

    public List<Party> getLenders(){
        return Collections.unmodifiableList(lenders);
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        List<AbstractParty> participants = new ArrayList<>(lenders);
        participants.add(borrower);
        return participants;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return uniqueIdentifier;
    }
}