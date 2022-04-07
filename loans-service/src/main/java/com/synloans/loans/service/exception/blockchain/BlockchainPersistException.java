package com.synloans.loans.service.exception.blockchain;

public class BlockchainPersistException extends RuntimeException{
    public BlockchainPersistException() {
    }

    public BlockchainPersistException(String message) {
        super(message);
    }

    public BlockchainPersistException(String message, Throwable cause) {
        super(message, cause);
    }

    public BlockchainPersistException(Throwable cause) {
        super(cause);
    }
}
