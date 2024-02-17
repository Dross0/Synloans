package com.synloans.loans.service.exception.document;

public class ContractNotFoundException extends RuntimeException {
    public ContractNotFoundException() {
    }

    public ContractNotFoundException(String message) {
        super(message);
    }

    public ContractNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContractNotFoundException(Throwable cause) {
        super(cause);
    }
}
