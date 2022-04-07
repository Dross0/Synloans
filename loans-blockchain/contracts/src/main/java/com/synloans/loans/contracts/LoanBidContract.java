package com.synloans.loans.contracts;


import com.synloans.loans.requirements.RequirementsUtils;
import com.synloans.loans.states.LoanBidState;
import com.synloans.loans.status.LoanBidStatus;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;


@Slf4j
public class LoanBidContract implements Contract {

    public static final String ID = "net.corda.samples.lending.contracts.LoanBidContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        CommandData commandData = tx.getCommands().get(0).getValue();
        if (commandData instanceof LoanBidContract.Commands.Submit) {
            verifySubmitCommand(tx);
        } else if (commandData instanceof LoanBidContract.Commands.Approve) {
            verifyApproveCommand(tx);
        } else {
            log.error("Unknown command - {}", commandData.getClass().getName());
            throw new UnsupportedOperationException(commandData.getClass().getName() + " - unknown");
        }
    }

    private void verifyApproveCommand(LedgerTransaction tx) {
        /* At here, the loan bid is verified for approval process. These contract rules make
        sure that all the conditions are met for the borrower to approve the sole loan bid for its
        project. */
        RequirementsUtils.requireThat(tx.getInputStates().size() == 1, "Expected one input state");

        List<LoanBidState> inputLoanBidStates = tx.inputsOfType(LoanBidState.class);
        RequirementsUtils.requireThat(!inputLoanBidStates.isEmpty(), "Expected LoanBidState input state");

        RequirementsUtils.requireThat(tx.getOutputStates().size() == 1, "Expected one output state");

        List<LoanBidState> outputLoanBidStates = tx.outputsOfType(LoanBidState.class);
        RequirementsUtils.requireThat(!outputLoanBidStates.isEmpty(), "Expected LoanBidState output state");

        LoanBidState inputLoanBidState = inputLoanBidStates.get(0);
        LoanBidState outputLoanBidState = outputLoanBidStates.get(0);

        verifyApproveLoanBid(inputLoanBidState, outputLoanBidState);

        verifyLoanBidState(tx, outputLoanBidState, LoanBidStatus.APPROVED);
    }

    private void verifySubmitCommand(LedgerTransaction tx){
        /* At here, the loan bid proposal from the competing banks is verified.
            These contract rules make sure that each loan bid for project is valid. */
        RequirementsUtils.requireThat(tx.getInputStates().isEmpty(), "Expected no input states");

        RequirementsUtils.requireThat(tx.getOutputStates().size() == 1, "Expected one output state");

        List<LoanBidState> loanBidContracts = tx.outputsOfType(LoanBidState.class);
        RequirementsUtils.requireThat(!loanBidContracts.isEmpty(), "Expected LoanBidState state");

        LoanBidState loanBidState = loanBidContracts.get(0);

        verifyLoanBidState(tx, loanBidState, LoanBidStatus.SUBMITTED);
    }

    private void verifyApproveLoanBid(LoanBidState inputLoanBidState, LoanBidState outputLoanBidState) {
        RequirementsUtils.requireThat(
                inputLoanBidState.getLoanAmount() == outputLoanBidState.getLoanAmount(),
                "Input and output loan amount must be equal"
        );

        RequirementsUtils.requireThat(
                inputLoanBidState.getTenure() == outputLoanBidState.getTenure(),
                "Input and output tenure must be equal"
        );

        RequirementsUtils.requireDoubleEquals(
                inputLoanBidState.getRateOfInterest(),
                outputLoanBidState.getRateOfInterest(),
                "Input and output rate of interest must be equal"
        );

        RequirementsUtils.requireEquals(
                inputLoanBidState.getBorrower(),
                outputLoanBidState.getBorrower(),
                "Input and output borrower must be same"
        );

        RequirementsUtils.requireEquals(
               inputLoanBidState.getLender(),
                outputLoanBidState.getLender(),
                "Input and output borrower must be same"
        );

        RequirementsUtils.requireThat(
                inputLoanBidState.getStatus() == LoanBidStatus.SUBMITTED,
                "Input state status must be " + LoanBidStatus.SUBMITTED
        );

        RequirementsUtils.requireThat(
                outputLoanBidState.getStatus() == LoanBidStatus.APPROVED,
                "Output state status must be " + LoanBidStatus.APPROVED
        );
    }

    private void verifyLoanBidState(LedgerTransaction tx, LoanBidState loanBidState, LoanBidStatus expectedStatus) {
        RequirementsUtils.requireThat(loanBidState.getStatus() == expectedStatus, "Expected " + expectedStatus + " status of bid");

        RequirementsUtils.requireThat(loanBidState.getLoanAmount() > 0, "Loan amount must be positive");

        RequirementsUtils.requireNotNull(loanBidState.getBorrower(), "Borrower must be not null");

        RequirementsUtils.requireNotNull(loanBidState.getLender(), "Lender must be not null");
//        ProjectState projectState = loanBidState.getProjectDetails()
//                .resolve(tx)
//                .getState()
//                .getData();
//        requireThat(projectState.getLenders().contains(loanBidState.getLender()), "Lender must be at project lenders");
//
//        requireEquals(
//                loanBidState.getBorrower(),
//                projectState.getBorrower(),
//                "Borrower at loan bid and project must be same"
//        );
//
//        requireThat(projectState.getLoanAmount() >= loanBidState.getLoanAmount(), "Loan bid must be less or equal all load");
    }


    public interface Commands extends CommandData {

        class Submit implements Commands {}

        class Approve implements Commands {}
    }
}
