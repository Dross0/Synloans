package com.synloans.loans.kafka;

import com.synloans.loans.model.dto.validation.ContractValidationMessage;

public interface ValidationProducer {

    void sendValidationMessage(ContractValidationMessage validationMessage);

}
