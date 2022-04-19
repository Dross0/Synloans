package com.synloans.loans.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BankParticipantInfo {
    private Long id;

    private String name;

    private LoanSum sum;

    private long issuedSum;

    private boolean approveBankAgent;
}
