package com.synloans.loans.adapter.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@RequiredArgsConstructor
public class BankJoinRequest {

    @NotNull(message = "Bank info cant be null")
    private final NodeUserInfo bank;

    @NotNull(message = "Loan id cant be null")
    private final LoanId loanId;

    @Positive(message = "Bank issued loan sum must be positive")
    private final long issuedLoanSum;

}
