package com.synloans.loans.service.messaging.impl;

import com.synloans.loans.document.extractor.DocumentExtractor;
import com.synloans.loans.kafka.ValidationProducer;
import com.synloans.loans.mapper.validation.ContractValidationMessageMapper;
import com.synloans.loans.model.dto.validation.ContractValidationMessage;
import com.synloans.loans.model.entity.document.Contract;
import com.synloans.loans.service.messaging.ContractValidationMessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractValidationMessagingServiceImpl implements ContractValidationMessagingService {

    private final ContractValidationMessageMapper contractValidationMessageMapper;

    private final DocumentExtractor documentExtractor;

    private final ValidationProducer validationProducer;

    @Override
    public void validateContract(Contract contract) {
        String contractText = documentExtractor.getText(contract.getDocument());
        ContractValidationMessage validationMessage = contractValidationMessageMapper.convert(contract, contractText);
        log.info("Send to validation message={}", validationMessage);
        validationProducer.sendValidationMessage(validationMessage);
    }

}
