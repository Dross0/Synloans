package com.synloans.loans.service.token.decoder.impl;

import com.synloans.loans.configuration.properties.security.JwtProperties;
import com.synloans.loans.model.authentication.token.impl.JwtToken;
import com.synloans.loans.service.token.decoder.TokenDecoder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtDecoder implements TokenDecoder {

    private final JwtProperties jwtProperties;

    @Override
    public JwtToken decode(String tokenValue) {
        Claims claims = extractClaims(tokenValue);

        return new JwtToken(
                tokenValue,
                claims.getSubject(),
                claims.getExpiration().toInstant(),
                claims.getIssuedAt().toInstant()
        );
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getKey())
                .parseClaimsJws(token)
                .getBody();
    }
}
