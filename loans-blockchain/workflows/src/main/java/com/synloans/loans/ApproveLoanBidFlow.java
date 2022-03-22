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

public final class ApproveLoanBidFlow {

    private ApproveLoanBidFlow(){
        throw new UnsupportedOperationException("Utility class constructor");
    }

    @InitiatingFlow
    @StartableByRPC
    @RequiredArgsConstructor
    public static class Initiator extends FlowLogic<SignedTransaction> {

        private final UniqueIdentifier bidIdentifier;


        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {

            StateAndRef<LoanBidState> inputStateAndRef = getLoanBidStateById(bidIdentifier);

            LoanBidState inputState = inputStateAndRef.getState().getData();

            LoanBidState output = new LoanBidState(
                    new StaticPointer<>(inputStateAndRef.getRef(), ProjectState.class),
                    inputState.getLinearId(),
                    inputState.getLender(),
                    inputState.getBorrower(),
                    inputState.getLoanAmount(),
                    inputState.getTenure(),
                    inputState.getRateOfInterest(),
                    LoanBidStatus.APPROVED
            );

            Party notary = inputStateAndRef.getState().getNotary();

            // Step 3. Create a new TransactionBuilder object.
            TransactionBuilder builder = new TransactionBuilder(notary);

            // Step 4. Add the inputs and outputs, as well as a command to the transaction builder.
            builder.addInputState(inputStateAndRef);
            builder.addOutputState(output);
            builder.addCommand(new LoanBidContract.Commands.Approve(), Arrays.asList(inputState.getBorrower().getOwningKey(),inputState.getLender().getOwningKey()));

            // Step 5. Verify and sign it with our KeyPair.
            builder.verify(getServiceHub());
            SignedTransaction ptx = getServiceHub().signInitialTransaction(builder);

            FlowSession cpSession = initiateFlow(inputState.getLender());

            //step 6: collect signatures
            SignedTransaction stx = subFlow(new CollectSignaturesFlow(ptx, Arrays.asList(cpSession)));


            // Step 7. Assuming no exceptions, we can now finalise the transaction
            return subFlow(new FinalityFlow(stx, Arrays.asList(cpSession)));
        }

        private StateAndRef<LoanBidState> getLoanBidStateById(UniqueIdentifier loanBidId) {
            return getServiceHub()
                    .getVaultService()
                    .queryBy(LoanBidState.class)
                    .getStates()
                    .stream()
                    .filter(loanBidStateAndRef -> loanBidStateAndRef.getState()
                            .getData()
                            .getLinearId()
                            .equals(loanBidId)
                    )
                    .findAny()
                    .orElseThrow(
                            () -> new IllegalArgumentException("Loan Bid with id = " + loanBidId + " Not Found")
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
            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(counterpartySession) {
                @Suspendable
                @Override
                protected void checkTransaction(SignedTransaction stx) throws FlowException {
                    /*
                     * SignTransactionFlow will automatically verify the transaction and its signatures before signing it.
                     * However, just because a transaction is contractually valid doesn’t mean we necessarily want to sign.
                     * What if we don’t want to deal with the counterparty in question, or the value is too high,
                     * or we’re not happy with the transaction’s structure? checkTransaction
                     * allows us to define these additional checks. If any of these conditions are not met,
                     * we will not sign the transaction - even if the transaction and its signatures are contractually valid.
                     * ----------
                     * For this hello-world cordapp, we will not implement any aditional checks.
                     * */
                }
            });
            //Stored the transaction into data base.
            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            return null;
        }
    }

}
