package com.synloans.loans.controller.document.contract;

import com.synloans.loans.model.dto.document.contract.ContractAttachRequest;
import com.synloans.loans.model.dto.document.contract.ContractDto;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ContractController {

    ContractDto attachContract(long loanRequestId, ContractAttachRequest attachRequest);

    List<ContractDto> getContracts(long loanRequestId);

    ContractDto getContract(long contractId);

    ResponseEntity<Resource> getContractContent(long contractId);

    void deleteContract(long contractId);

}
