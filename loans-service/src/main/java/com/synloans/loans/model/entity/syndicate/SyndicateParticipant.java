package com.synloans.loans.model.entity.syndicate;

import com.synloans.loans.model.entity.company.Bank;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "syndicate_participant")
@Getter
@Setter
public class SyndicateParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bank_id")
    private Bank bank;

    @ManyToOne
    @JoinColumn(name = "syndicate_id")
    private Syndicate syndicate;

    @Column(name = "loan_sum")
    private Long loanSum;

    @Column(name = "issued_loan_sum")
    private Long issuedLoanSum;

    @Column(name = "approve_bank_agent")
    private boolean approveBankAgent;
}
