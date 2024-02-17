package com.synloans.loans.kafka.impl;

import com.synloans.loans.configuration.properties.ContractValidationProperties;
import com.synloans.loans.kafka.ValidationProducer;
import com.synloans.loans.model.dto.validation.ContractValidationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidationProducerImpl implements ValidationProducer {

    private final KafkaTemplate<String, ContractValidationMessage> validationKafkaTemplate;

    private final ContractValidationProperties contractValidationProperties;

    @Override
    public void sendValidationMessage(ContractValidationMessage validationMessage) {
        validationKafkaTemplate.send(contractValidationProperties.getTopic(), validationMessage);
    }
}
