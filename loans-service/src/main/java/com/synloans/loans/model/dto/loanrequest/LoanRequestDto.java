package com.synloans.loans.model.dto.loanrequest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class LoanRequestDto {
    private long sum;

    private double maxRate;

    private int term;
}
