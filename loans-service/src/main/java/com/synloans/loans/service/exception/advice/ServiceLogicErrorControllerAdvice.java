package com.synloans.loans.service.exception.advice;

import com.synloans.loans.service.exception.AcceptPaymentException;
import com.synloans.loans.service.exception.SyndicateJoinException;
import com.synloans.loans.service.exception.advice.response.ErrorResponse;
import com.synloans.loans.service.exception.blockchain.BlockchainPersistException;
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
public class ServiceLogicErrorControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler({
            SyndicateJoinException.class,
            AcceptPaymentException.class,
            BlockchainPersistException.class
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

}
