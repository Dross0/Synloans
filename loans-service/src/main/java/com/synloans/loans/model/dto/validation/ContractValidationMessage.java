package com.synloans.loans.model.dto.validation;

import com.synloans.loans.model.entity.document.ContractType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class ContractValidationMessage {

    private long contractId;

    private ContractType contractType;

    private Instant contractDate;

    private String contractText;

    private LoanRequestValidation loanRequest;

}
