package com.synloans.loans.configuration.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = BlockchainServiceProperties.PREFIX)
@ConstructorBinding
@Getter
@RequiredArgsConstructor
public class BlockchainServiceProperties {

    public static final String PREFIX = "blockchain.service";

    private final String host;
}
