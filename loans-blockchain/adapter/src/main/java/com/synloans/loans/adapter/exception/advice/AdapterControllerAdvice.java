package com.synloans.loans.adapter.exception.advice;

import com.synloans.loans.adapter.exception.BankJoinException;
import com.synloans.loans.adapter.exception.LoanCreateException;
import com.synloans.loans.adapter.exception.PartyResolveException;
import com.synloans.loans.adapter.exception.PaymentException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class AdapterControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {
            LoanCreateException.class,
            BankJoinException.class,
            PaymentException.class
    })
    public ResponseEntity<Object> handleInternalError(RuntimeException ex, WebRequest request){
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );
        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value = {PartyResolveException.class})
    public ResponseEntity<Object> handleNotFoundError(RuntimeException ex, WebRequest request){
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );
        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
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

        return handleExceptionInternal(ex, errorResponse, headers, HttpStatus.BAD_REQUEST, request);
    }
}
