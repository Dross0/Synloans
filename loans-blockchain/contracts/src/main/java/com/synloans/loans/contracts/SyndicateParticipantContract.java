package com.synloans.loans.contracts;

import com.synloans.loans.states.LoanState;
import com.synloans.loans.states.SyndicateParticipantState;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static com.synloans.loans.requirements.RequirementsUtils.requirePositive;
import static com.synloans.loans.requirements.RequirementsUtils.requireThat;

@Slf4j
public class SyndicateParticipantContract implements Contract {
    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        CommandData commandData = tx.getCommands().get(0).getValue();
        if (commandData instanceof Commands.Submit) {
            verifySubmitCommand(tx);
        } else {
            log.error("Unknown command - {}", commandData.getClass().getName());
            throw new UnsupportedOperationException(commandData.getClass().getName() + " - unknown");
        }
    }

    private void verifySubmitCommand(LedgerTransaction tx) {
        requireThat(tx.getInputStates().isEmpty(), "Expected no input state");

        requireThat(tx.getOutputStates().size() == 1, "Expected one output state");

        List<SyndicateParticipantState> outputSyndicateParticipant = tx.outputsOfType(SyndicateParticipantState.class);
        requireThat(!outputSyndicateParticipant.isEmpty(), "Expected SyndicateParticipantState output state");

        verifySubmittedSyndicateParticipant(outputSyndicateParticipant.get(0), tx);
    }

    private void verifySubmittedSyndicateParticipant(SyndicateParticipantState state, LedgerTransaction tx) {

        requirePositive(state.getIssuedLoanSum(), "Issued sum must be positive");

        StateAndRef<LoanState> resolve = state.getLoanState().resolve(tx);
        LoanState loanState = resolve.getState().getData();
        requireThat(!Objects.equals(state.getBank(), loanState.getBorrower()), "Bank cant be borrower");

        requireThat(loanState.getBanks().contains(state.getBank()), "Bank must be at bank list");

        requireThat(loanState.getLoanSum() >= state.getIssuedLoanSum(), "Issued sum cant be greater than loan sum");
    }


    public interface Commands extends CommandData {

        class Submit implements Commands {}

        //TODO возможно подтверждение от банка агента class Approve implements ProjectContract.Commands {}
    }
}
