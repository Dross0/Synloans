package com.sinloans.loans.model.dto.loanrequest;

import com.sinloans.loans.model.dto.BankParticipantInfo;
import com.sinloans.loans.model.dto.CompanyDto;
import lombok.*;

import java.util.Collection;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoanRequestResponse {
    private LoanRequestInfo info;

    private Collection<BankParticipantInfo> banks;

    private CompanyDto borrower;
}
