package com.sinloans.loans.model.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class LoanRequestDto {
    private LoanSum sum;

    private double maxRate;

    private int term;
}
