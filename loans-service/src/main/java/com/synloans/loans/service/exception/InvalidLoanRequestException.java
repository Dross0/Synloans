package com.synloans.loans.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Неверная заявка на кредит")
public class InvalidLoanRequestException extends RuntimeException{
    public InvalidLoanRequestException() {
        super();
    }

    public InvalidLoanRequestException(String message) {
        super(message);
    }

    public InvalidLoanRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidLoanRequestException(Throwable cause) {
        super(cause);
    }
}
