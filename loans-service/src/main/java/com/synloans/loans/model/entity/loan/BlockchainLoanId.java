package com.synloans.loans.model.entity.loan;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "blockchain_loan_ids")
@Getter
@Setter
@NoArgsConstructor
public class BlockchainLoanId {

    @Id
    private Long id;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "primary_id")
    private UUID primaryId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private Loan loan;
}
