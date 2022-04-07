package com.synloans.loans.contracts;

import com.synloans.loans.states.LoanState;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.synloans.loans.requirements.RequirementsUtils.requirePositive;
import static com.synloans.loans.requirements.RequirementsUtils.requireThat;

@Slf4j
public class LoanContract implements Contract {
    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        CommandData commandData = tx.getCommands().get(0).getValue();
        if (commandData instanceof LoanContract.Commands.Create) {
            verifyCreateCommand(tx);
        } else {
            log.error("Unknown command - {}", commandData.getClass().getName());
            throw new UnsupportedOperationException(commandData.getClass().getName() + " - unknown");
        }
    }

    private void verifyCreateCommand(LedgerTransaction tx) {
        requireThat(tx.getInputStates().isEmpty(), "Expected no input state");

        requireThat(tx.getOutputStates().size() == 1, "Expected one output state");

        List<LoanState> outputLoanState = tx.outputsOfType(LoanState.class);
        requireThat(!outputLoanState.isEmpty(), "Expected LoanState output state");

        verifyCreatedLoanState(outputLoanState.get(0));
    }

    private void verifyCreatedLoanState(LoanState loanState){
        requirePositive(loanState.getLoanSum(), "Loan sum must be positive");

        requirePositive(loanState.getTerm(), "Loan term must be positive");

        requirePositive(loanState.getRate(), "Loan rate must be positive");

        requireThat(!loanState.getBanks().isEmpty(), "Must be at least one bank");

        requireThat(loanState.getBanks().contains(loanState.getAgentBank()), "Agent bank must be at banks list");
    }

    public interface Commands extends CommandData {

        class Create implements Commands {}

        class Close implements Commands {}

    }
}
