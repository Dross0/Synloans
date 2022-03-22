package com.synloans.loans.states;

import com.synloans.loans.contracts.LoanBidContract;
import com.synloans.loans.status.LoanBidStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.StaticPointer;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(LoanBidContract.class)
@RequiredArgsConstructor
@Getter
public class LoanBidState implements LinearState {

    private final StaticPointer<ProjectState> projectDetails;

    private final UniqueIdentifier uniqueIdentifier;

    private final Party lender;

    private final Party borrower;

    private final long loanAmount;

    private final long tenure;

    private final double rateOfInterest;

    private final LoanBidStatus status;


    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(lender, borrower);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return uniqueIdentifier;
    }
}
