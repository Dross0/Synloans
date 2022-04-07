package com.synloans.loans.service.blockchain.url;

import java.net.URI;

public interface BlockchainServiceUrlFactory {

    URI getLoanCreatePostUrl();

    URI getBankJoinPostUrl();

    URI getPaymentPostUrl();

}
