package com.synloans.loans.scheduler.impl;

import com.synloans.loans.scheduler.ContractValidationScheduler;
import com.synloans.loans.service.contract.validation.ContractValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContractValidationSchedulerImpl implements ContractValidationScheduler {

    private final ContractValidationService contractValidationService;


    @Scheduled(fixedDelayString = "${contract.validation.delay}")
    @Override
    public void validateNewContracts() {
        contractValidationService.validateNewContacts();
    }
}
