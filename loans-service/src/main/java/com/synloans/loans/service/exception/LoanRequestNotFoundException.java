package com.synloans.loans.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Заявка на кредит не найдена")
public class LoanRequestNotFoundException extends RuntimeException{
    public LoanRequestNotFoundException() {
        super();
    }

    public LoanRequestNotFoundException(String message) {
        super(message);
    }

    public LoanRequestNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoanRequestNotFoundException(Throwable cause) {
        super(cause);
    }
}
