package com.synloans.loans.adapter.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class LoanId {

    private final String loanExternalId;

    @NotNull(message = "id не может отсутствовать")
    private final UUID id;

}
