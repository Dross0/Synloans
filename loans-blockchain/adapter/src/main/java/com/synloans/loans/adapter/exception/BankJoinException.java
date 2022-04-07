package com.synloans.loans.adapter.exception;

public class BankJoinException extends RuntimeException{
    public BankJoinException() {
    }

    public BankJoinException(String message) {
        super(message);
    }

    public BankJoinException(String message, Throwable cause) {
        super(message, cause);
    }

    public BankJoinException(Throwable cause) {
        super(cause);
    }
}
