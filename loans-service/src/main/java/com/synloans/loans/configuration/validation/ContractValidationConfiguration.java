package com.synloans.loans.configuration.validation;

import com.synloans.loans.configuration.properties.ContractValidationProperties;
import com.synloans.loans.model.dto.validation.ContractValidationMessage;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@EnableConfigurationProperties(ContractValidationProperties.class)
public class ContractValidationConfiguration {


    @Bean
    public KafkaTemplate<String, ContractValidationMessage> validationMessageKafkaTemplate(ProducerFactory<String, ContractValidationMessage> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

}
