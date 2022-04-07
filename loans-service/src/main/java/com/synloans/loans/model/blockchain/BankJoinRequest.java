package com.synloans.loans.model.blockchain;

import com.synloans.loans.model.dto.NodeUserInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BankJoinRequest {

    private final NodeUserInfo bank;

    private final LoanId loanId;

    private final long issuedLoanSum;

}
