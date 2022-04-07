package com.synloans.loans.adapter.exception;

public class PartyResolveException extends RuntimeException{
    public PartyResolveException() {
    }

    public PartyResolveException(String message) {
        super(message);
    }

    public PartyResolveException(String message, Throwable cause) {
        super(message, cause);
    }

    public PartyResolveException(Throwable cause) {
        super(cause);
    }
}
