package com.synloans.loans;

import co.paralleluniverse.fibers.Suspendable;
import com.synloans.loans.contracts.SyndicateBidContract;
import com.synloans.loans.states.SyndicateBidState;
import com.synloans.loans.states.SyndicateState;
import com.synloans.loans.status.SyndicateBidStatus;
import lombok.RequiredArgsConstructor;
import net.corda.core.contracts.LinearPointer;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.Arrays;

public final class SubmitSyndicateBidFlow {

    private SubmitSyndicateBidFlow(){
        throw new UnsupportedOperationException("Utility class constructor");
    }

    @InitiatingFlow
    @StartableByRPC
    @RequiredArgsConstructor
    public static class Initiator extends FlowLogic<SignedTransaction> {

        private final UniqueIdentifier syndicateIdentifier;

        private final long bidAmount;


        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {

            StateAndRef<SyndicateState> syndicateStateAndRef = getSyndicateStateById(syndicateIdentifier);

            SyndicateState syndicateState = syndicateStateAndRef.getState().getData();

            SyndicateBidState syndicateBidState = new SyndicateBidState(
                    new UniqueIdentifier(),
                    new LinearPointer<>(syndicateIdentifier, SyndicateState.class),
                    bidAmount,
                    syndicateState.getLeadBank(),
                    getOurIdentity(),
                    SyndicateBidStatus.SUBMITTED
            );

            Party notary = syndicateStateAndRef.getState().getNotary();

            // Step 3. Create a new TransactionBuilder object.
            final TransactionBuilder builder = new TransactionBuilder(notary);

            // Step 4. Add the project as an output state, as well as a command to the transaction builder.
            builder.addOutputState(syndicateBidState);
            builder.addCommand(new SyndicateBidContract.Commands.Submit(), Arrays.asList(getOurIdentity().getOwningKey()));

            // Step 5. Verify and sign it with our KeyPair.
            builder.verify(getServiceHub());
            final SignedTransaction ptx = getServiceHub().signInitialTransaction(builder);

            FlowSession cpSession = initiateFlow(syndicateState.getLeadBank());

            // Step 7. Assuming no exceptions, we can now finalise the transaction
            return subFlow(new FinalityFlow(ptx, Arrays.asList(cpSession)));
        }

        private StateAndRef<SyndicateState> getSyndicateStateById(UniqueIdentifier syndicateId){
            return getServiceHub()
                    .getVaultService()
                    .queryBy(SyndicateState.class)
                    .getStates()
                    .stream()
                    .filter(synStateAndRef -> synStateAndRef.getState()
                            .getData()
                            .getLinearId()
                            .equals(syndicateId)
                    )
                    .findAny()
                    .orElseThrow(
                            () -> new IllegalArgumentException("Syndicate with id = " + syndicateId + " Not Found")
                    );
        }
    }

    @InitiatedBy(Initiator.class)
    @RequiredArgsConstructor
    public static class Responder extends FlowLogic<Void> {

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
