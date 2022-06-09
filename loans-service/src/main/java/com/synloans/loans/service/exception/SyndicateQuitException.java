package com.synloans.loans.service.exception;

public class SyndicateQuitException extends RuntimeException {
    public SyndicateQuitException() {
        super();
    }

    public SyndicateQuitException(String message) {
        super(message);
    }

    public SyndicateQuitException(String message, Throwable cause) {
        super(message, cause);
    }

    public SyndicateQuitException(Throwable cause) {
        super(cause);
    }
}
