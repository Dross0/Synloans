package com.synloans.loans.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SyndicateJoinRequest {
    private long requestId;

    private long sum;

    private boolean approveBankAgent;
}
