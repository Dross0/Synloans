package com.synloans.loans.model.entity.loan;

import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.model.entity.loan.payment.ActualPayment;
import com.synloans.loans.model.entity.loan.payment.PlannedPayment;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "loan")
@Getter
@Setter
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sum")
    private Long sum;

    @Column(name = "rate")
    private Double rate;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "close_date")
    private LocalDate closeDate;

    @OneToOne
    @JoinColumn(name = "request")
    private LoanRequest request;

    @ManyToOne
    @JoinColumn(name = "bank_agent")
    private Bank bankAgent;

    @OneToMany(mappedBy = "loan")
    private List<PlannedPayment> plannedPayments;

    @OneToMany(mappedBy = "loan")
    private List<ActualPayment> actualPayments;

    @OneToOne(
            mappedBy = "loan",
            cascade = CascadeType.ALL
    )
    private BlockchainLoanId blockchainLoanId;

    public void setBlockchainLoanId(BlockchainLoanId blockchainLoanId) {
        if (blockchainLoanId == null){
            if (this.blockchainLoanId != null){
                this.blockchainLoanId.setLoan(null);
            }
        } else {
            blockchainLoanId.setLoan(this);
        }
        this.blockchainLoanId = blockchainLoanId;
    }
}
