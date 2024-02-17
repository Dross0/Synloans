package com.synloans.loans.service.contract;

import com.synloans.loans.model.dto.document.contract.ContractAttachRequest;
import com.synloans.loans.model.dto.document.contract.ContractDto;
import com.synloans.loans.model.entity.document.Contract;
import com.synloans.loans.model.entity.document.ContractStatus;
import com.synloans.loans.model.entity.user.User;

import java.util.List;

public interface ContractService {

    ContractDto attachContract(long loanRequestId, ContractAttachRequest contractAttachRequest, User user);

    List<ContractDto> getContracts(long loanRequestId, User currentUser);

    void delete(long contractId, User currentUser);

    ContractDto getContract(long contractId, User currentUser);

    List<Contract> getContractsByStatus(int count, ContractStatus status);
}
