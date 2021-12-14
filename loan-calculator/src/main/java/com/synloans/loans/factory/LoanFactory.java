package com.synloans.loans.factory;


import com.synloans.loans.Loan;
import com.synloans.loans.info.LoanInfo;
import org.jetbrains.annotations.NotNull;

public interface LoanFactory {
    @NotNull
    Loan create(@NotNull final LoanInfo loanInfo);
}