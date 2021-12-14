package com.synloans.loans.model.entity.loan.payment;

import com.synloans.loans.model.entity.loan.Loan;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "planned_payment")
@Getter
@Setter
public class PlannedPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "principal")
    private BigDecimal principal;

    @Column(name = "percent")
    private BigDecimal percent;

    @Column(name = "date")
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;
}
