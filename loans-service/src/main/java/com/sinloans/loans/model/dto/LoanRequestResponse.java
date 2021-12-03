package com.sinloans.loans.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class LoanRequestResponse {
    private long id;

    private LocalDate dateIssue;

    private LocalDate dateCreate;

    private LoanSum sum;

    private double maxRate;

    private int term;
}
