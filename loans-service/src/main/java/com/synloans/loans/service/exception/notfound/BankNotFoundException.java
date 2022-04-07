package com.synloans.loans.service.exception.notfound;

import com.synloans.loans.service.exception.notfound.base.NotFoundException;

public class BankNotFoundException extends NotFoundException {
    public BankNotFoundException() {
    }

    public BankNotFoundException(String message) {
        super(message);
    }

    public BankNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public BankNotFoundException(Throwable cause) {
        super(cause);
    }
}
