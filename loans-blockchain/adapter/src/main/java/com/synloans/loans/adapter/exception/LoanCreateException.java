package com.synloans.loans.adapter.exception;

public class LoanCreateException extends RuntimeException {
    public LoanCreateException() {
    }

    public LoanCreateException(String message) {
        super(message);
    }

    public LoanCreateException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoanCreateException(Throwable cause) {
        super(cause);
    }
}
