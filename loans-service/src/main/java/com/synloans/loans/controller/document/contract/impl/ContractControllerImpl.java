package com.synloans.loans.controller.document.contract.impl;

import com.synloans.loans.configuration.api.Api;
import com.synloans.loans.controller.document.contract.ContractController;
import com.synloans.loans.mapper.document.DocumentMapper;
import com.synloans.loans.model.dto.document.DocumentDto;
import com.synloans.loans.model.dto.document.contract.ContractAttachRequest;
import com.synloans.loans.model.dto.document.contract.ContractDto;
import com.synloans.loans.service.contract.ContractService;
import com.synloans.loans.service.document.DocumentService;
import com.synloans.loans.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(Api.V1 + Api.LOAN_REQUEST + "/{loanRequestId}" + Api.CONTRACT)
@RequiredArgsConstructor
public class ContractControllerImpl implements ContractController {

    public static final String LOAN_REQUEST_ID = "loanRequestId";
    private final ContractService contractService;

    private final UserService userService;

    private final DocumentService documentService;

    private final DocumentMapper documentMapper;

    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ContractDto attachContract(
            @PathVariable(LOAN_REQUEST_ID) long loanRequestId,
            @RequestBody ContractAttachRequest attachRequest
    ) {
        return contractService.attachContract(loanRequestId, attachRequest, userService.getCurrentUser());
    }

    @GetMapping("/")
    @Override
    public List<ContractDto> getContracts(@PathVariable(LOAN_REQUEST_ID) long loanRequestId) {
        return contractService.getContracts(loanRequestId, userService.getCurrentUser());
    }

    @GetMapping("/{contractId}")
    @Override
    public ContractDto getContract(@PathVariable("contractId") long contractId) {
        return contractService.getContract(contractId, userService.getCurrentUser());
    }

    @GetMapping("/{contractId}/content")
    @Override
    public ResponseEntity<Resource> getContractContent(@PathVariable("contractId") long contractId) {
        ContractDto contract = contractService.getContract(contractId, userService.getCurrentUser());
        DocumentDto document = documentService.getDocument(contract.getDocumentId());
        return documentMapper.convertToResource(document);
    }

    @DeleteMapping("/{contractId}")
    @Override
    public void deleteContract(@PathVariable("contractId") long contractId) {
        contractService.delete(contractId, userService.getCurrentUser());
    }
}
