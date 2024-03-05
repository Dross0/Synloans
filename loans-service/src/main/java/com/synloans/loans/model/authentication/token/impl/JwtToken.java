package com.synloans.loans.model.authentication.token.impl;

import com.synloans.loans.model.authentication.token.Token;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Instant;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class JwtToken implements Token {

    private final String token;

    @Getter
    private final String username;

    @Getter
    private final Instant expiration;

    @Getter
    private final Instant issueTime;

    @Override
    public String getValue() {
        return token;
    }
}
