package com.synloans.loans.model.dto.loan;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class LoanCreateResponse {

    private final long loanRequestId;

    private final long loanId;

}
