package com.synloans.loans.adapter.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class LoanCreateRequest {

    @NotNull(message = "Bank agent info cant be null")
    private NodeUserInfo bankAgent;

    @NotBlank(message = "Borrower cant be blank")
    private String borrower;

    @Positive(message = "Loan sum must be positive")
    private long loanSum;

    @Positive(message = "Loan term must be positive")
    private int term;

    @Positive(message = "Loan rate must be positive")
    private double rate;

    @NotEmpty(message = "Must be at least one bank")
    private List<String> banks;

}
