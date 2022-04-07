package com.synloans.loans;

import co.paralleluniverse.fibers.Suspendable;
import com.synloans.loans.contracts.LoanPaymentContract;
import com.synloans.loans.states.LoanPaymentState;
import com.synloans.loans.states.LoanState;
import lombok.RequiredArgsConstructor;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.StaticPointer;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.List;
import java.util.stream.Collectors;

public final class LoanPaymentFlow {

    private LoanPaymentFlow(){
        throw new UnsupportedOperationException("Utility class constructor");
    }

    @InitiatingFlow
    @StartableByRPC
    @RequiredArgsConstructor
    public static class Initiator extends FlowLogic<SignedTransaction> {

        public static final String NOTARY = "O=Notary,L=London,C=GB";

        private final UniqueIdentifier loanId;

        private final long payment;

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {

            // Step 1. Get a reference to the notary service on our network and our key pair.
            /** Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)*/
            Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse(NOTARY));
            if (notary == null) {
                throw new IllegalStateException("Cant found notary: " + NOTARY);
            }


            Party borrower = getOurIdentity();

            StateAndRef<LoanState> loan = getLoanById(loanId);
            LoanState loanState = loan.getState().getData();
            LoanPaymentState paymentState = new LoanPaymentState(
                    new UniqueIdentifier(),
                    new StaticPointer<>(loan.getRef(), LoanState.class, true),
                    borrower,
                    payment,
                    loanState.getBanks()
            );


            // Step 3. Create a new TransactionBuilder object.
            TransactionBuilder builder = new TransactionBuilder(notary);

            // Step 4. Add the project as an output state, as well as a command to the transaction builder.
            builder.addOutputState(paymentState);
            builder.addCommand(
                    new LoanPaymentContract.Commands.Pay(),
                    paymentState.getParticipants().stream()
                            .map(AbstractParty::getOwningKey)
                            .collect(Collectors.toList())
            );

            // Step 5. Verify and sign it with our KeyPair.
            builder.verify(getServiceHub());
            SignedTransaction ptx = getServiceHub().signInitialTransaction(builder);

            List<FlowSession> cpSessions = loanState.getBanks().stream()
                    .map(this::initiateFlow)
                    .collect(Collectors.toList());

            SignedTransaction stx = subFlow(new CollectSignaturesFlow(ptx, cpSessions));

            // Step 7. Assuming no exceptions, we can now finalise the transaction
            return subFlow(new FinalityFlow(stx, cpSessions));
        }

        private StateAndRef<LoanState> getLoanById(UniqueIdentifier loadId) {
            return getServiceHub()
                    .getVaultService()
                    .queryBy(LoanState.class)
                    .getStates()
                    .stream()
                    .filter(
                            loanStateAndRef -> loanStateAndRef.getState()
                                    .getData()
                                    .getLinearId()
                                    .equals(loadId)
                    )
                    .findAny()
                    .orElseThrow(
                            () -> new IllegalArgumentException("Loan with id = " + loadId + " not found")
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
