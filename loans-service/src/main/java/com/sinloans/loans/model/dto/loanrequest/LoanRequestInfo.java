package com.sinloans.loans.model.dto.loanrequest;

import com.sinloans.loans.model.dto.LoanSum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Builder
@Getter
@Setter
public class LoanRequestInfo {
    private long id;

    private LocalDate dateIssue;

    private LocalDate dateCreate;

    private LoanSum sum;

    private double maxRate;

    private int term;

    private LoanRequestStatus status;
}
