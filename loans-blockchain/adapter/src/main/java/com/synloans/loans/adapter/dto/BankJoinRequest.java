package com.synloans.loans.adapter.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@EqualsAndHashCode
public class BankJoinRequest {

    @Valid
    @NotNull(message = "Bank info cant be null")
    private final NodeUserInfo bank;

    @Valid
    @NotNull(message = "Loan id cant be null")
    private final LoanId loanId;

    @Positive(message = "Bank issued loan sum must be positive")
    private final long issuedLoanSum;

    @JsonCreator
    public BankJoinRequest(
            @JsonProperty(value = "bank", required = true) NodeUserInfo bank,
            @JsonProperty(value = "loanId", required = true) LoanId loanId,
            @JsonProperty(value = "issuedLoanSum", required = true) long issuedLoanSum
    ){
        this.bank = bank;
        this.loanId = loanId;
        this.issuedLoanSum = issuedLoanSum;
    }

}
