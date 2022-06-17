package com.synloans.loans.service.exception.advice.base;

import com.synloans.loans.service.exception.AcceptPaymentException;
import com.synloans.loans.service.exception.CreateUserException;
import com.synloans.loans.service.exception.ForbiddenResourceException;
import com.synloans.loans.service.exception.InvalidLoanRequestException;
import com.synloans.loans.service.exception.SyndicateJoinException;
import com.synloans.loans.service.exception.SyndicateQuitException;
import com.synloans.loans.service.exception.UserUnauthorizedException;
import com.synloans.loans.service.exception.advice.response.ErrorResponse;
import com.synloans.loans.service.exception.blockchain.BlockchainPersistException;
import com.synloans.loans.service.exception.notfound.base.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class BaseControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {NotFoundException.class})
    public ResponseEntity<Object> handleNotFoundError(RuntimeException ex, WebRequest request){
        log.error("Not found exception handle at controller advice", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );
        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), errorResponse.getStatus(), request);
    }


    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<Object> handleGlobalError(Exception ex, WebRequest request){
        log.error("Exception handle at controller advice", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );
        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), errorResponse.getStatus(), request);
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("Validation exception handle at controller advice", ex);
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Request validation errors",
                String.join("; ", errors)
        );

        return handleExceptionInternal(ex, errorResponse, headers, errorResponse.getStatus(), request);
    }

    @ExceptionHandler({
            UserUnauthorizedException.class,
            CreateUserException.class
    })
    public ResponseEntity<Object> handleUnauthorizedError(RuntimeException ex, WebRequest request){
        log.error("Unauthorized user exception handle at controller advice", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );
        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), errorResponse.getStatus(), request);
    }

    @ExceptionHandler({
            SyndicateJoinException.class,
            AcceptPaymentException.class,
            BlockchainPersistException.class,
            SyndicateQuitException.class
    })
    public ResponseEntity<Object> handleServiceLogicError(RuntimeException ex, WebRequest request){
        log.error("Service logic exception handle at controller advice", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );
        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), errorResponse.getStatus(), request);
    }

    @ExceptionHandler({
            InvalidLoanRequestException.class
    })
    public ResponseEntity<Object> handleBadRequestError(RuntimeException ex, WebRequest request){
        log.error("Service logic bad request exception handle at controller advice", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );
        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), errorResponse.getStatus(), request);
    }

    @ExceptionHandler({
            ForbiddenResourceException.class,
            AccessDeniedException.class
    })
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
