package com.synloans.loans;

import co.paralleluniverse.fibers.Suspendable;
import com.synloans.loans.contracts.SyndicateContract;
import com.synloans.loans.states.LoanBidState;
import com.synloans.loans.states.ProjectState;
import com.synloans.loans.states.SyndicateState;
import lombok.RequiredArgsConstructor;
import net.corda.core.contracts.LinearPointer;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.StatesToRecord;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class CreateSyndicateFlow {

    private CreateSyndicateFlow(){
        throw new UnsupportedOperationException("Utility class constructor");
    }

    @InitiatingFlow
    @StartableByRPC
    @RequiredArgsConstructor
    public static class Initiator extends FlowLogic<SignedTransaction> {

        public static final String NOTARY = "O=Notary,L=London,C=GB";

        private final List<Party> participantBanks;

        private final UniqueIdentifier projectIdentifier;

        private final UniqueIdentifier loanDetailIdentifier;


        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {

            // Step 1. Get a reference to the notary service on our network and our key pair.
            /** Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)*/
            Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse(NOTARY));
            if (notary == null) {
                throw new IllegalStateException("Cant found notary: " + NOTARY);
            }


            Party leadBank = getOurIdentity();

            fetchProject(projectIdentifier);

            fetchLoanBid(loanDetailIdentifier);

            SyndicateState syndicateState = new SyndicateState(
                    new UniqueIdentifier(),
                    leadBank,
                    participantBanks,
                    new LinearPointer<>(projectIdentifier, ProjectState.class),
                    new LinearPointer<>(loanDetailIdentifier, LoanBidState.class)
            );


            // Step 3. Create a new TransactionBuilder object.
            TransactionBuilder builder = new TransactionBuilder(notary);

            // Step 4. Add the project as an output state, as well as a command to the transaction builder.
            builder.addOutputState(syndicateState);
            builder.addCommand(new SyndicateContract.Commands.Create(), Arrays.asList(syndicateState.getLeadBank().getOwningKey()));

            // Step 5. Verify and sign it with our KeyPair.
            builder.verify(getServiceHub());
            SignedTransaction ptx = getServiceHub().signInitialTransaction(builder);

            List<FlowSession> cpSessions = participantBanks.stream().map(this::initiateFlow).collect(Collectors.toList());

            // Step 7. Assuming no exceptions, we can now finalise the transaction
            return subFlow(new FinalityFlow(ptx, cpSessions));
        }

        private void fetchProject(UniqueIdentifier projectId){
            getServiceHub()
                    .getVaultService()
                    .queryBy(ProjectState.class)
                    .getStates()
                    .stream()
                    .filter(projectStateAndRef -> projectStateAndRef.getState()
                            .getData()
                            .getLinearId()
                            .equals(projectId)
                    )
                    .findAny()
                    .orElseThrow(
                            () -> new IllegalArgumentException("Project Not Found")
                    );
        }

        private void fetchLoanBid(UniqueIdentifier loanDetailId){
            getServiceHub()
                    .getVaultService()
                    .queryBy(LoanBidState.class)
                    .getStates()
                    .stream()
                    .filter(loanBidStateAndRef -> loanBidStateAndRef.getState()
                            .getData()
                            .getLinearId()
                            .equals(loanDetailId)
                    )
                    .findAny()
                    .orElseThrow(
                            () -> new IllegalArgumentException("Loan Details Not Found")
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
            subFlow(new ReceiveFinalityFlow(counterpartySession, null, StatesToRecord.ALL_VISIBLE));
            return null;
        }
    }

}
