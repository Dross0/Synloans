package com.synloans.loans.factory;

import com.synloans.loans.AnnuityLoan;
import com.synloans.loans.info.LoanInfo;
import org.jetbrains.annotations.NotNull;

public class AnnuityLoanFactory implements LoanFactory {
    @Override
    public @NotNull AnnuityLoan create(@NotNull LoanInfo loanInfo) {
        return new AnnuityLoan(loanInfo);
    }
}
