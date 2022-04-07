package com.synloans.loans.adapter.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class LoanId {

    private final String loanExternalId;

    private final UUID id;

}
