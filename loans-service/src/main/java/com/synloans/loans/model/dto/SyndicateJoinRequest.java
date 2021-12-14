package com.synloans.loans.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Positive;

@NoArgsConstructor
@Getter
@Setter
public class SyndicateJoinRequest {
    private long requestId;

    @Positive(message = "Сумма должна быть положительной")
    private long sum;

    private boolean approveBankAgent;
}
