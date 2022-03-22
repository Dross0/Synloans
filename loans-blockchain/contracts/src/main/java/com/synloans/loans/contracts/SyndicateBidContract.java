package com.synloans.loans.contracts;

import com.synloans.loans.requirements.RequirementsUtils;
import com.synloans.loans.states.SyndicateBidState;
import com.synloans.loans.status.SyndicateBidStatus;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Slf4j
public class SyndicateBidContract implements Contract {

    public static final String ID = "net.corda.samples.lending.contracts.SyndicateBidContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        CommandData commandData = tx.getCommands().get(0).getValue();

        if (commandData instanceof SyndicateBidContract.Commands.Submit) {
            verifySubmitCommand(tx);
        } else if (commandData instanceof SyndicateBidContract.Commands.Approve) {
            verifyApproveCommand(tx);
        } else {
            log.error("Unknown command - {}", commandData.getClass().getName());
            throw new UnsupportedOperationException(commandData.getClass().getName() + " - unknown");
        }
    }

    private void verifyApproveCommand(LedgerTransaction tx) {
        /* At here, the syndicated bid is verified for approval process. These contract rules make
        sure that all the conditions are met for the lead bank to approve the each syndicated bid. */
        List<ContractState> inputStates = tx.getInputStates();
        RequirementsUtils.requireThat(inputStates.size() == 1, "Expected 1 input state");

        List<SyndicateBidState> inputSyndicateBidStates = tx.inputsOfType(SyndicateBidState.class);
        RequirementsUtils.requireThat(!inputSyndicateBidStates.isEmpty(), "Expected SyndicateBidState input state");

        RequirementsUtils.requireThat(tx.getOutputStates().size() == 1, "Expected one output state");


        List<SyndicateBidState> outputSyndicateBidStates = tx.outputsOfType(SyndicateBidState.class);
        RequirementsUtils.requireThat(!outputSyndicateBidStates.isEmpty(), "Expected SyndicateBidState output state");

        SyndicateBidState inputSyndicateBidState = inputSyndicateBidStates.get(0);
        SyndicateBidState outputSyndicateBidState = outputSyndicateBidStates.get(0);

        verifyApproveSyndicateBid(inputSyndicateBidState, outputSyndicateBidState);

        verifySyndicateBidState(inputSyndicateBidState, SyndicateBidStatus.APPROVED);

    }

    private void verifySubmitCommand(LedgerTransaction tx) {
        /* At here, the syndication bid proposal from the syndication participating banks is verified.
        These contract rules make sure that each bid for syndicated loan is valid. */
        RequirementsUtils.requireThat(tx.getInputStates().isEmpty(), "Expected no input states");

        RequirementsUtils.requireThat(tx.getOutputStates().size() == 1, "Expected one output state");

        List<SyndicateBidState> outputSyndicateBidStates = tx.outputsOfType(SyndicateBidState.class);
        RequirementsUtils.requireThat(!outputSyndicateBidStates.isEmpty(), "Excepted SyndicateBidState state");


        SyndicateBidState outputSyndicateBidState = outputSyndicateBidStates.get(0);

        verifySyndicateBidState(outputSyndicateBidState, SyndicateBidStatus.SUBMITTED);
    }

    private void verifySyndicateBidState(SyndicateBidState syndicateBidState, SyndicateBidStatus expectedStatus) {
        RequirementsUtils.requireThat(syndicateBidState.getStatus() == expectedStatus, "Expected " + expectedStatus + " status of bid");

        RequirementsUtils.requireThat(syndicateBidState.getBidAmount() > 0, "Bid amount must be positive");

        RequirementsUtils.requireNotNull(syndicateBidState.getParticipantBank(), "Participant bank must be not null");

        RequirementsUtils.requireNotNull(syndicateBidState.getLeadBank(), "Lead bank must be not null");
    }

    private void verifyApproveSyndicateBid(SyndicateBidState input, SyndicateBidState output) {
        RequirementsUtils.requireThat(
                input.getBidAmount() == output.getBidAmount(),
                "Input and output bid amount must be equal"
        );

        RequirementsUtils.requireEquals(
                input.getLeadBank(),
                output.getLeadBank(),
                "Input and output lead bank must be same"
        );

        RequirementsUtils.requireEquals(
                input.getParticipantBank(),
                output.getParticipantBank(),
                "Input and output participant bank must be same"
        );

        RequirementsUtils.requireThat(
                input.getStatus() == SyndicateBidStatus.SUBMITTED,
                "Input state status must be" + SyndicateBidStatus.SUBMITTED
        );

        RequirementsUtils.requireThat(
                output.getStatus() == SyndicateBidStatus.APPROVED,
                "Output state status must be " + SyndicateBidStatus.APPROVED
        );
    }


    public interface Commands extends CommandData {

        class Submit implements Commands {}

        class Approve implements Commands {}

    }
}
