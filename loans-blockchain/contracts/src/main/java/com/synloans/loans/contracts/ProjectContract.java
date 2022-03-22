package com.synloans.loans.contracts;

import com.synloans.loans.states.ProjectState;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.transactions.LedgerTransaction;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.synloans.loans.requirements.RequirementsUtils.requireNotNull;
import static com.synloans.loans.requirements.RequirementsUtils.requireThat;


@Slf4j
public class ProjectContract implements Contract {

    public static final String ID = "net.corda.samples.lending.contracts.ProjectContract";


    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        CommandData commandData = tx.getCommands().get(0).getValue();

        if (commandData instanceof ProjectContract.Commands.ProposeProject) {
            verifyProposeProjectCommand(tx);
        } else {
            log.error("Unknown command - {}", commandData.getClass().getName());
            throw new UnsupportedOperationException(commandData.getClass().getName() + " - unknown");
        }
    }

    private void verifyProposeProjectCommand(LedgerTransaction tx) {
        /*At here, you can structure the rules for creating a project proposal
         * this verify method makes sure that all proposed projects from the borrower company
         * are sound, so that banks are not going to waste any time on unqualified project proposals*/
        List<ContractState> inputStates = tx.getInputStates();
        requireThat(inputStates.isEmpty(), "Expected no input states");

        requireThat(tx.getOutputs().size() == 1, "Expected one output state");

        List<ProjectState> projectStates = tx.outputsOfType(ProjectState.class);
        requireThat(!projectStates.isEmpty(), "Expected ProjectState state");

        ProjectState projectState = projectStates.get(0);

        verifyProjectState(projectState);
    }

    private void verifyProjectState(ProjectState projectState) {
        requireThat(projectState.getLoanAmount() > 0, "Loan amount must be positive");

        requireThat(StringUtils.isNotBlank(projectState.getProjectDescription()), "Project description cant be blank");

        requireNotNull(projectState.getBorrower(), "Borrower cant be null");

        requireThat(!projectState.getLenders().isEmpty(), "Must be at least one lender");
    }


    public interface Commands extends CommandData {

        class ProposeProject implements Commands {}

    }
}