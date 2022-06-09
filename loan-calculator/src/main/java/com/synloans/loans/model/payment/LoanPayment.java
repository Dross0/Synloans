package com.synloans.loans.model.payment;


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
