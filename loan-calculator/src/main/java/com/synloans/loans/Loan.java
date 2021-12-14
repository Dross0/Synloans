package com.synloans.loans;

import com.synloans.loans.info.LoanInfo;
import com.synloans.loans.payment.LoanPayment;
import lombok.NonNull;
import org.javamoney.moneta.Money;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Loan {
    @NotNull
    LoanInfo getInfo();

    Money getMonthlyPayment(int monthNumber);

    Money getTotalPayout();

    Money getMonthlyCreditBalance(int monthNumber);

    @NonNull Money getMonthlyPrincipalPayout(int monthNumber);

    @NonNull Money getMonthlyPercentPayout(int monthNumber);

    Money getTotalPercentPayout();

    @NotNull
    LoanPayment getPaymentByMonthNumber(int monthNum);

    @NotNull
    List<LoanPayment> getPaymentsList();
}
