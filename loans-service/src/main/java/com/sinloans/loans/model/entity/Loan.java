package com.sinloans.loans.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
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
}
