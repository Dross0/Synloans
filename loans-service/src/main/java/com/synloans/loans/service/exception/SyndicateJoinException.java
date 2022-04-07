package com.synloans.loans.service.exception;

public class SyndicateJoinException extends RuntimeException{
    public SyndicateJoinException() {
    }

    public SyndicateJoinException(String message) {
        super(message);
    }

    public SyndicateJoinException(String message, Throwable cause) {
        super(message, cause);
    }

    public SyndicateJoinException(Throwable cause) {
        super(cause);
    }
}
