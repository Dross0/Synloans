package com.synloans.loans.configuration.money;

import com.synloans.loans.configuration.properties.MoneyRoundProperties;
import org.javamoney.moneta.function.MonetaryOperators;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.money.MonetaryOperator;

@Configuration
@EnableConfigurationProperties(MoneyRoundProperties.class)
public class MoneyRoundConfiguration {

    @Bean
    MonetaryOperator roundOperator(MoneyRoundProperties moneyRoundProperties){
        return MonetaryOperators.rounding(moneyRoundProperties.getScale());
    }
}
