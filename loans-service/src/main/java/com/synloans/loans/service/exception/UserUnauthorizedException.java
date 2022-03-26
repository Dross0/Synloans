package com.synloans.loans.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Текущий пользователь не найден")
public class UserUnauthorizedException extends RuntimeException{
    public UserUnauthorizedException() {
    }

    public UserUnauthorizedException(String message) {
        super(message);
    }

    public UserUnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserUnauthorizedException(Throwable cause) {
        super(cause);
    }
}
