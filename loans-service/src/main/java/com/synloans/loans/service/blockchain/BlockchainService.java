package com.synloans.loans.service.blockchain;

import com.synloans.loans.model.blockchain.BankJoinRequest;
import com.synloans.loans.model.blockchain.LoanCreateRequest;
import com.synloans.loans.model.blockchain.LoanId;
import com.synloans.loans.model.blockchain.PaymentBlockchainRequest;

public interface BlockchainService {

    LoanId createLoan(LoanCreateRequest loanCreateRequest);

    void joinBank(BankJoinRequest bankJoinRequest);

    void makePayment(PaymentBlockchainRequest paymentRequest);

}
