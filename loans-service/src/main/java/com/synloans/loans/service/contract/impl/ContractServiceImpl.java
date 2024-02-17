package com.synloans.loans.service.contract.impl;

import com.synloans.loans.mapper.document.contract.ContractMapper;
import com.synloans.loans.model.dto.document.contract.ContractAttachRequest;
import com.synloans.loans.model.dto.document.contract.ContractDto;
import com.synloans.loans.model.entity.document.Contract;
import com.synloans.loans.model.entity.document.ContractStatus;
import com.synloans.loans.model.entity.document.Document;
import com.synloans.loans.model.entity.loan.LoanRequest;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.repository.document.ContractRepository;
import com.synloans.loans.service.contract.ContractService;
import com.synloans.loans.service.document.DocumentService;
import com.synloans.loans.service.exception.ForbiddenResourceException;
import com.synloans.loans.service.exception.document.ContractNotFoundException;
import com.synloans.loans.service.loan.LoanRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractServiceImpl implements ContractService {

    private final DocumentService documentService;

    private final ContractRepository contractRepository;

    private final LoanRequestService loanRequestService;

    private final ContractMapper contractMapper;

    @Override
    public ContractDto attachContract(long loanRequestId, ContractAttachRequest contractAttachRequest, User user) {
        Document document = documentService.findById(contractAttachRequest.getDocumentId());
        if (!documentService.hasPermission(document, user)) {
            log.error("User with id={} has no permission to document={}", user.getId(), document.getId());
            throw new ForbiddenResourceException("No permissions to use document");
        }
        LoanRequest loanRequest = loanRequestService.getOwnedCompanyLoanRequestById(loanRequestId, user.getCompany());
        Contract contract = Contract.builder()
                .attachedAt(Instant.now())
                .type(contractAttachRequest.getType())
                .status(ContractStatus.NEW)
                .document(document)
                .loanRequest(loanRequest)
                .build();
        return contractMapper.convert(contractRepository.save(contract));
    }

    @Override
    public List<ContractDto> getContracts(long loanRequestId, User currentUser) {
        LoanRequest loanRequest = loanRequestService.getOwnedCompanyLoanRequestById(loanRequestId, currentUser.getCompany());
        return loanRequest.getContracts()
                .stream()
                .map(contractMapper::convert)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void delete(long contractId, User currentUser) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException("No contract with id=" + contractId));

        if (!hasDeletePermission(contract, currentUser)) {
            log.error("User with id={} has no permission to change contract={}", currentUser.getId(), contract.getId());
            throw new ForbiddenResourceException("No permissions to delete this contract");
        }
        contractRepository.delete(contract);
    }

    @Override
    public ContractDto getContract(long contractId, User currentUser) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException("No contract with id=" + contractId));

        if (!hasReadPermissionToContract(contract, currentUser)) {
            log.error("User with id={} has no permission to contract={}", currentUser.getId(), contract.getId());
            throw new ForbiddenResourceException("Cant get contract from unrelated request");
        }

        return contractMapper.convert(contract);
    }

    @Override
    public List<Contract> getContractsByStatus(int count, ContractStatus status) {
        return contractRepository.findByStatus(status, Pageable.ofSize(count));
    }

    private boolean hasReadPermissionToContract(Contract contract, User user) {
        return Objects.equals(contract.getLoanRequest().getCompany(), user.getCompany()) ||
                loanRequestService.isLoanRequestParticipant(contract.getLoanRequest(), user.getCompany());
    }

    private boolean hasDeletePermission(Contract contract, User user) {
        return documentService.hasPermission(contract.getDocument(), user) &&
                contract.getLoanRequest().getLoan() == null;
    }
}
