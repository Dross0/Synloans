package com.synloans.loans.factory;

import com.synloans.loans.DifferentiatedLoan;
import com.synloans.loans.info.LoanInfo;
import org.jetbrains.annotations.NotNull;

public class DifferentiatedLoanFactory implements LoanFactory {
    @Override
    public @NotNull DifferentiatedLoan create(@NotNull LoanInfo loanInfo) {
        return new DifferentiatedLoan(loanInfo);
    }
}
