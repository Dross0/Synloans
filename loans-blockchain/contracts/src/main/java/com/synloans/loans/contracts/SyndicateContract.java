package com.synloans.loans.contracts;

import com.synloans.loans.states.SyndicateState;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.synloans.loans.requirements.RequirementsUtils.requireNotNull;
import static com.synloans.loans.requirements.RequirementsUtils.requireThat;


@Slf4j
public class SyndicateContract implements Contract {

    public static final String ID = "net.corda.samples.lending.contracts.SyndicateContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        final CommandData commandData = tx.getCommands().get(0).getValue();

        if (commandData instanceof SyndicateContract.Commands.Create) {
            verifyCreateCommand(tx);
        } else {
            log.error("Unknown command - {}", commandData.getClass().getName());
            throw new UnsupportedOperationException(commandData.getClass().getName() + " - unknown");
        }
    }

    private void verifyCreateCommand(LedgerTransaction tx) {
        /*Here writes the rules for the lead bank's creating the syndication.*/

        requireThat(tx.getInputStates().isEmpty(), "Expected no input states");

        requireThat(tx.getOutputStates().size() == 1, "Expected 1 output state");

        List<SyndicateState> syndicateStates = tx.outputsOfType(SyndicateState.class);
        requireThat(!syndicateStates.isEmpty(), "Expected SyndicateState state");

        SyndicateState syndicateState = syndicateStates.get(0);

        verifySyndicateState(syndicateState);
    }

    private void verifySyndicateState(SyndicateState syndicateState) {
        requireNotNull(syndicateState.getLeadBank(), "Lead bank must be not null");
        //TODO verify
    }

    public interface Commands extends CommandData {

        class Create implements Commands {}

    }
}
