package com.synloans.loans.service.contract.validation.impl;

import com.synloans.loans.model.entity.document.Contract;
import com.synloans.loans.model.entity.document.ContractStatus;
import com.synloans.loans.service.contract.ContractService;
import com.synloans.loans.service.contract.validation.ContractValidationService;
import com.synloans.loans.service.messaging.ContractValidationMessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractValidationServiceImpl implements ContractValidationService {

    private static final int VALIDATION_BUCKET_SIZE = 10;

    private final ContractService contractService;

    private final ContractValidationMessagingService contractValidationMessagingService;

    @Transactional
    @Override
    public void validateNewContacts() {
        List<Contract> newContacts = contractService.getContractsByStatus(VALIDATION_BUCKET_SIZE, ContractStatus.NEW);
        for (Contract contract: newContacts) {
            contractValidationMessagingService.validateContract(contract);
            contract.setStatus(ContractStatus.VALIDATING);
        }
    }
}
