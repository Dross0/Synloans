package com.synloans.loans.adapter.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class PaymentRequest {

    @Valid
    @NotNull(message = "Payer info cant be null")
    private final NodeUserInfo payer;

    @Valid
    @NotNull(message = "Loan id cant be null")
    private final LoanId loanId;

    @Positive(message = "Payment sum must be positive")
    private final long payment;

}
