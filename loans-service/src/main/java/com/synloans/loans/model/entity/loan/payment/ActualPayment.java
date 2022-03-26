package com.synloans.loans.model.entity.loan.payment;

import com.synloans.loans.model.entity.loan.Loan;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "actual_payment")
@Getter
@Setter
public class ActualPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment")
    private Long payment;

    @Column(name = "date")
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;
}
