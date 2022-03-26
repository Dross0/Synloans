package com.synloans.loans.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY, reason = "Не удалось зафиксировать платеж")
public class AcceptPaymentException extends RuntimeException{
    public AcceptPaymentException() {
    }

    public AcceptPaymentException(String message) {
        super(message);
    }

    public AcceptPaymentException(String message, Throwable cause) {
        super(message, cause);
    }

    public AcceptPaymentException(Throwable cause) {
        super(cause);
    }
}
