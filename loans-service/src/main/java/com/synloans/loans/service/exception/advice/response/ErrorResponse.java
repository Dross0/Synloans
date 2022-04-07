package com.synloans.loans.service.exception.advice.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@RequiredArgsConstructor
@Getter
public class ErrorResponse {

    private final HttpStatus status;

    private final String error;

    private final String message;

    private final Instant timestamp = Instant.now();

    public int getCode(){
        return status.value();
    }

}
