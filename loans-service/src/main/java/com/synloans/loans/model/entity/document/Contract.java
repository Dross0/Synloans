package com.synloans.loans.model.entity.document;

import com.synloans.loans.model.entity.loan.LoanRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "loan_document")
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ContractType type;

    @Column(name = "attached_at")
    private Instant attachedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ContractStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "loand_request_id", referencedColumnName = "id")
    private LoanRequest loanRequest;
}
