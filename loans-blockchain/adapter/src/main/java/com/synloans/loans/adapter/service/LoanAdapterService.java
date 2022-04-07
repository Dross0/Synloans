package com.synloans.loans.adapter.service;

import com.synloans.loans.adapter.dto.BankJoinRequest;
import com.synloans.loans.adapter.dto.LoanCreateRequest;
import com.synloans.loans.adapter.dto.LoanId;
import com.synloans.loans.adapter.dto.PaymentRequest;

public interface LoanAdapterService {

    LoanId createLoan(LoanCreateRequest loanInfo);

    void joinBank(BankJoinRequest joinRequest);

    void payLoan(PaymentRequest paymentRequest);
}
