package com.synloans.loans.model.blockchain;

import com.synloans.loans.model.dto.NodeUserInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class LoanCreateRequest {

    private NodeUserInfo bankAgent;

    private String borrower;

    private long loanSum;

    private int term;

    private double rate;

    private List<String> banks;

}
