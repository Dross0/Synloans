package com.synloans.loans.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.validation.constraints.Positive;

@Getter
@EqualsAndHashCode
public class SyndicateJoinRequest {
    private final long requestId;

    @Positive(message = "Сумма должна быть положительной")
    private final long sum;

    private final boolean approveBankAgent;

    @JsonCreator
    public SyndicateJoinRequest(
            @JsonProperty(value = "requestId", required = true) long requestId,
            @JsonProperty(value = "sum", required = true) long sum,
            @JsonProperty(value = "approveBankAgent", required = true) boolean approveBankAgent
    ) {
        this.requestId = requestId;
        this.approveBankAgent = approveBankAgent;
        this.sum = sum;
    }
}
