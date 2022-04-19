package com.synloans.loans.configuration.properties.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.time.Duration;

@ConfigurationProperties(prefix = JwtProperties.PREFIX)
@ConstructorBinding
@Getter
@RequiredArgsConstructor
public class JwtProperties {
    public static final String PREFIX = "jwt";

    private final String key;

    private final Duration expirationTime;
}
