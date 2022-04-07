package com.synloans.loans.contracts;

import com.synloans.loans.states.LoanPaymentState;
import com.synloans.loans.states.LoanState;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.synloans.loans.requirements.RequirementsUtils.*;

@Slf4j
public class LoanPaymentContract implements Contract {
    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        CommandData commandData = tx.getCommands().get(0).getValue();
        if (commandData instanceof Commands.Pay) {
            verifyPayCommand(tx);
        } else {
            log.error("Unknown command - {}", commandData.getClass().getName());
            throw new UnsupportedOperationException(commandData.getClass().getName() + " - unknown");
        }
    }

    private void verifyPayCommand(LedgerTransaction tx) {
        requireThat(tx.getInputStates().isEmpty(), "Expected no input state");

        requireThat(tx.getOutputStates().size() == 1, "Expected one output state");

        List<LoanPaymentState> outputPaymentState = tx.outputsOfType(LoanPaymentState.class);
        requireThat(!outputPaymentState.isEmpty(), "Expected LoanPaymentState output state");

        verifyPayment(outputPaymentState.get(0), tx);
    }

    private void verifyPayment(LoanPaymentState loanPaymentState, LedgerTransaction tx) {
        requirePositive(loanPaymentState.getPayment(), "Payment must be positive");

        LoanState loanState = loanPaymentState.getLoanState().resolve(tx).getState().getData();

        requireEquals(loanState.getBorrower(), loanPaymentState.getBorrower(), "Loan borrower and payment company must be same");
    }

    public interface Commands extends CommandData {

        class Pay implements Commands {}

    }
}
