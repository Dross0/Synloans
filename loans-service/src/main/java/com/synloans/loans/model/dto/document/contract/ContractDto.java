package com.synloans.loans.model.dto.document.contract;

import com.synloans.loans.model.entity.document.ContractStatus;
import com.synloans.loans.model.entity.document.ContractType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ContractDto {

    private long contractId;

    private long loanRequestId;

    private ContractType type;

    private ContractStatus status;

    private UUID documentId;

    private Instant attachedAt;

}
