package com.synloans.loans.service.token.decoder.impl;

import com.synloans.loans.configuration.properties.security.JwtProperties;
import com.synloans.loans.model.authentication.token.impl.JwtToken;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JwtDecoder.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JwtDecoderTest {

    @Autowired
    JwtDecoder jwtDecoder;

    @MockBean
    JwtProperties jwtProperties;

    @Test
    @DisplayName("Декодирование JWT из строки")
    void decodeTest(){
        String username = "user1";
        Instant issue = Instant.now().minusSeconds(23);
        Instant expiration = Instant.now().plus(Duration.ofDays(1));

        String secretKey = "secret";
        when(jwtProperties.getKey()).thenReturn(secretKey);

        String jwtStr = Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(username)
                .setIssuedAt(Date.from(issue))
                .setExpiration(Date.from(expiration))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();

        JwtToken token = jwtDecoder.decode(jwtStr);

        assertThat(token.getValue()).isEqualTo(jwtStr);
        assertThat(token.getUsername()).isEqualTo(username);
        assertThat(token.getIssueTime().getEpochSecond()).isEqualTo(issue.getEpochSecond());
        assertThat(token.getExpiration().getEpochSecond()).isEqualTo(expiration.getEpochSecond());
    }

    @Test
    @DisplayName("Декодирование просроченного JWT из строки")
    void decodeExpiredTest(){
        String username = "user1";
        Instant issue = Instant.now().minusSeconds(23);
        Instant expiration = Instant.now().minusSeconds(1);

        String secretKey = "secret";
        when(jwtProperties.getKey()).thenReturn(secretKey);

        String jwtStr = Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(username)
                .setIssuedAt(Date.from(issue))
                .setExpiration(Date.from(expiration))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();

        Throwable throwable = catchThrowable(() -> jwtDecoder.decode(jwtStr));
        assertThat(throwable).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("Декодирование навалидного JWT из строки")
    void decodeInvalidTest(){
        String secretKey = "secret";
        when(jwtProperties.getKey()).thenReturn(secretKey);

        String invalidJwt = "fefeojogjketetet";

        Throwable throwable = catchThrowable(() -> jwtDecoder.decode(invalidJwt));
        assertThat(throwable).isInstanceOf(JwtException.class);
    }

}