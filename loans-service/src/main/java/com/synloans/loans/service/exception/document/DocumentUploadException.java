package com.synloans.loans.service.exception.document;

public class DocumentUploadException extends RuntimeException {
    public DocumentUploadException() {
    }

    public DocumentUploadException(String message) {
        super(message);
    }

    public DocumentUploadException(String message, Throwable cause) {
        super(message, cause);
    }

    public DocumentUploadException(Throwable cause) {
        super(cause);
    }
}
