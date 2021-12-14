package com.synloans.loans.payment;


import lombok.NonNull;
import lombok.Value;
import org.javamoney.moneta.Money;

import java.time.LocalDate;


@Value
public class LoanPayment {
    @NonNull PaymentSum paymentSum;
    @NonNull Money loanBalance;
    @NonNull LocalDate date;
}
