package com.synloans.loans;

import co.paralleluniverse.fibers.Suspendable;
import com.synloans.loans.contracts.LoanBidContract;
import com.synloans.loans.states.LoanBidState;
import com.synloans.loans.states.ProjectState;
import com.synloans.loans.status.LoanBidStatus;
import lombok.RequiredArgsConstructor;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.StaticPointer;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.Arrays;

public final class SubmitLoanBidFlow {

    private SubmitLoanBidFlow(){
        throw new UnsupportedOperationException("Utility class constructor");
    }

    @InitiatingFlow
    @StartableByRPC
    @RequiredArgsConstructor
    public static class Initiator extends FlowLogic<SignedTransaction>{

        private final Party borrower;

        private final long loanAmount;

        private final long tenure;

        private final double rateOfInterest;

        private final UniqueIdentifier projectIdentifier;


        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            Party lender = getOurIdentity();

            StateAndRef<ProjectState> inputStateAndRef = getProjectStateById(projectIdentifier);

            Party notary = inputStateAndRef.getState().getNotary();

            LoanBidState output = new LoanBidState(
                    new StaticPointer<>(inputStateAndRef.getRef(), ProjectState.class),
                    new UniqueIdentifier(),
                    lender,
                    borrower,
                    loanAmount,
                    tenure,
                    rateOfInterest,
                    LoanBidStatus.SUBMITTED
            );

            // Step 3. Create a new TransactionBuilder object.
            TransactionBuilder builder = new TransactionBuilder(notary);

            // Step 4. Add the project as an output state, as well as a command to the transaction builder.
            builder.addOutputState(output);
            builder.addCommand(new LoanBidContract.Commands.Submit(), Arrays.asList(lender.getOwningKey()));

            // Step 5. Verify and sign it with our KeyPair.
            builder.verify(getServiceHub());
            SignedTransaction ptx = getServiceHub().signInitialTransaction(builder);

            FlowSession cpSession = initiateFlow(borrower);

            // Step 7. Assuming no exceptions, we can now finalise the transaction
            return subFlow(new FinalityFlow(ptx, Arrays.asList(cpSession)));
        }

        private StateAndRef<ProjectState> getProjectStateById(UniqueIdentifier projectId) {
            return getServiceHub()
                    .getVaultService()
                    .queryBy(ProjectState.class)
                    .getStates()
                    .stream()
                    .filter(
                            projectStateAndRef -> projectStateAndRef.getState()
                                    .getData()
                                    .getLinearId()
                                    .equals(projectId)
                    )
                    .findAny()
                    .orElseThrow(
                            () -> new IllegalArgumentException("Project with id = " + projectId + " Not Found")
                    );
        }
    }

    @InitiatedBy(Initiator.class)
    @RequiredArgsConstructor
    public static class Responder extends FlowLogic<Void>{

        private final FlowSession counterpartySession;

        @Suspendable
        @Override
        public Void call() throws FlowException {

            //Stored the transaction into data base.
            subFlow(new ReceiveFinalityFlow(counterpartySession));
            return null;
        }
    }

}
