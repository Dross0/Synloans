package com.synloans.loans.configuration.properties.blockchain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.time.Duration;

@ConfigurationProperties(prefix = BlockchainServiceProperties.PREFIX)
@ConstructorBinding
@Getter
@RequiredArgsConstructor
public class BlockchainServiceProperties {

    public static final String PREFIX = "blockchain.service";

    private final String host;

    private final Duration connectionTimeout;

    private final Duration readTimeout;
}
