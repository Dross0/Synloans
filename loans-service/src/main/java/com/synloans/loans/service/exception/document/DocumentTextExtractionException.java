package com.synloans.loans.service.exception.document;

public class DocumentTextExtractionException extends RuntimeException {
    public DocumentTextExtractionException() {
    }

    public DocumentTextExtractionException(String message) {
        super(message);
    }

    public DocumentTextExtractionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DocumentTextExtractionException(Throwable cause) {
        super(cause);
    }
}
