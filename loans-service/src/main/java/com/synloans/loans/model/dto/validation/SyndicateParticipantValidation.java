package com.synloans.loans.model.dto.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class SyndicateParticipantValidation {

    private CompanyValidation bank;

    private long sum;

    private boolean approveBankAgent;

}
