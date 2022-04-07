package com.synloans.loans.service.exception.notfound;

import com.synloans.loans.service.exception.notfound.base.NotFoundException;


public class LoanRequestNotFoundException extends NotFoundException {
    public LoanRequestNotFoundException() {
        super();
    }

    public LoanRequestNotFoundException(String message) {
        super(message);
    }

    public LoanRequestNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoanRequestNotFoundException(Throwable cause) {
        super(cause);
    }
}
