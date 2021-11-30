package com.sinloans.loans.security.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtConfig {
    private String key = UUID.randomUUID().toString();

    private long expirationTime = TimeUnit.SECONDS.convert(1, TimeUnit.DAYS);
}
