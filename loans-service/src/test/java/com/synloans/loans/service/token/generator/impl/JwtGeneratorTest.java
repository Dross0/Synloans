package com.synloans.loans.service.token.generator.impl;

import com.synloans.loans.configuration.properties.security.JwtProperties;
import com.synloans.loans.model.authentication.token.impl.JwtToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JwtGenerator.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JwtGeneratorTest {

    @Autowired
    JwtGenerator jwtGenerator;

    @MockBean
    JwtProperties jwtProperties;

    @Test
    @DisplayName("Создание JWT")
    void decodeTest(){
        String username = "user1";

        UserDetails userDetails = Mockito.mock(UserDetails.class);

        when(userDetails.getUsername()).thenReturn(username);

        Duration expirationTime = Duration.ofDays(1);
        String secretKey = "secret";
        when(jwtProperties.getKey()).thenReturn(secretKey);
        when(jwtProperties.getExpirationTime()).thenReturn(expirationTime);

        Instant beforeMethod = Instant.now();

        JwtToken jwtToken = jwtGenerator.generateToken(userDetails);

        assertThat(jwtToken.getValue()).isNotBlank();
        assertThat(jwtToken.getUsername()).isEqualTo(username);
        assertThat(jwtToken.getExpiration())
                .isAfterOrEqualTo(beforeMethod.plus(expirationTime))
                .isBeforeOrEqualTo(Instant.now().plus(expirationTime));
        assertThat(jwtToken.getIssueTime())
                .isAfterOrEqualTo(beforeMethod)
                .isBeforeOrEqualTo(Instant.now());
    }
}