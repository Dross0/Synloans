package com.synloans.loans.model.entity.loan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.document.Contract;
import com.synloans.loans.model.entity.syndicate.Syndicate;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.List;

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

    @OneToMany(mappedBy = "loanRequest")
    private List<Contract> contracts;
}
