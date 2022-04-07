package com.synloans.loans.model.blockchain;

import com.synloans.loans.model.dto.NodeUserInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PaymentBlockchainRequest {

    private final NodeUserInfo payer;

    private final LoanId loanId;

    private final long payment;

}
