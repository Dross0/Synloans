package com.synloans.loans.adapter.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@RequiredArgsConstructor
@Getter
public class PaymentRequest {

    @NotNull(message = "Payer info cant be null")
    private final NodeUserInfo payer;

    @NotNull(message = "Loan id cant be null")
    private final LoanId loanId;

    @Positive(message = "Payment sum must be positive")
    private final long payment;

}
