package com.synloans.loans.model.blockchain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.UUID;

@Getter
public class LoanId {

    private final String loanExternalId;

    private final UUID id;

    @JsonCreator
    public LoanId(
            @JsonProperty(value = "loanExternalId", required = true) String loanExternalId,
            @JsonProperty(value = "id", required = true) UUID id
    ){
        this.loanExternalId = loanExternalId;
        this.id = id;
    }

}
