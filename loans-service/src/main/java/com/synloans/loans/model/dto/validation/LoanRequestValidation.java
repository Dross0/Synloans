package com.synloans.loans.model.dto.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class LoanRequestValidation {

    private long sum;

    private int term;

    private double rate;

    private LocalDate createDate;

    private CompanyValidation borrower;

    private List<SyndicateParticipantValidation> syndicateParticipants;
}
