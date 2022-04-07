package com.synloans.loans.service.exception.notfound;

import com.synloans.loans.service.exception.notfound.base.NotFoundException;

public class LoanNotFoundException extends NotFoundException {

    public LoanNotFoundException() {
    }

    public LoanNotFoundException(String message) {
        super(message);
    }

    public LoanNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoanNotFoundException(Throwable cause) {
        super(cause);
    }
}
