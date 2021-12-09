package com.synloans.loans.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "loan")
@Getter
@Setter
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sum")
    private Double sum;

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
}
