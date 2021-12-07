package com.sinloans.loans.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "bank")
@Getter
@Setter
public class Bank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="company_info")
    private Company company;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "license")
    private Document license;

    @OneToMany(mappedBy = "bank")
    @JsonIgnore
    private Set<SyndicateParticipant> syndicates;

    @OneToMany(mappedBy = "bankAgent")
    @JsonIgnore
    private Set<Loan> loansAsBankAgent;
}
