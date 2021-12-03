package com.sinloans.loans.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "loan_request")
@Getter
@Setter
public class LoanRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sum")
    private long sum;

    @Column(name = "term")
    private int term;

    @Column(name = "rate")
    private double rate;

    @Column(name = "create_date")
    private LocalDate createDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company", nullable = false)
    private Company company;

    @OneToOne(mappedBy = "request")
    @JsonIgnore
    private Syndicate syndicate;

    @OneToOne(mappedBy = "request")
    @JsonIgnore
    private Loan loan;
}
