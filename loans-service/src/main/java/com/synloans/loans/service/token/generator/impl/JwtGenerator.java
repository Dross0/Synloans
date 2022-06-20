package com.synloans.loans.service.token.generator.impl;

import com.synloans.loans.configuration.properties.security.JwtProperties;
import com.synloans.loans.model.authentication.token.impl.JwtToken;
import com.synloans.loans.service.token.generator.TokenGenerator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtGenerator implements TokenGenerator {

    private final JwtProperties jwtProperties;

    @Override
    public JwtToken generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private JwtToken createToken(Map<String, Object> claims, String subject) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.getExpirationTime());

        String jwt = Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getKey())
                .compact();

        return new JwtToken(
                jwt,
                subject,
                expiration,
                now
        );
    }
}
