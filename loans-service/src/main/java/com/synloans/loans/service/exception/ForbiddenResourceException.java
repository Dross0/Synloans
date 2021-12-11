package com.synloans.loans.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Ресурс недоступен текущему пользователю")
public class ForbiddenResourceException extends RuntimeException{
    public ForbiddenResourceException() {
        super();
    }

    public ForbiddenResourceException(String message) {
        super(message);
    }

    public ForbiddenResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForbiddenResourceException(Throwable cause) {
        super(cause);
    }
}
