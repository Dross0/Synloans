package com.synloans.loans.status;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public enum LoanBidStatus {

    APPROVED,

    SUBMITTED,

    UNKNOWN

}
