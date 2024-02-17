package com.synloans.loans.configuration.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = ContractValidationProperties.PREFIX)
@ConstructorBinding
@Getter
@RequiredArgsConstructor
public class ContractValidationProperties {

    public static final String PREFIX = "contract.validation";

    private final String topic;

}
