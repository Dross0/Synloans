package com.synloans.loans.configuration.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.PositiveOrZero;

@ConfigurationProperties(prefix = MoneyRoundProperties.PREFIX)
@ConstructorBinding
@RequiredArgsConstructor
@Getter
@Validated
public class MoneyRoundProperties {

    public static final String PREFIX = "money.round";

    @PositiveOrZero
    private final int scale;
}
