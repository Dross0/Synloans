package com.synloans.loans.service.exception.advice;

import com.synloans.loans.service.exception.ForbiddenResourceException;
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
public class ForbiddenResourceControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = ForbiddenResourceException.class)
    public ResponseEntity<Object> handleForbiddenResourceException(RuntimeException ex, WebRequest request){
        log.error("Forbidden resource exception handle at controller advice", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN,
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );
        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), errorResponse.getStatus(), request);
    }

}
