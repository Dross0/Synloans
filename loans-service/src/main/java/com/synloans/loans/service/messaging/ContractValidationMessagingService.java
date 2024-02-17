package com.synloans.loans.service.messaging;

import com.synloans.loans.model.entity.document.Contract;

public interface ContractValidationMessagingService {

    void validateContract(Contract contract);

}
