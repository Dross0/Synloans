package com.synloans.loans.service.exception.advice;

import com.synloans.loans.service.exception.CreateUserException;
import com.synloans.loans.service.exception.UserUnauthorizedException;
import com.synloans.loans.service.exception.advice.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class UnauthorizedControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {UserUnauthorizedException.class, CreateUserException.class})
    public ResponseEntity<Object> handleUnauthorizedError(RuntimeException ex, WebRequest request){
        log.error("Unauthorized user exception handle at controller advice", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );
        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), errorResponse.getStatus(), request);
    }

}
