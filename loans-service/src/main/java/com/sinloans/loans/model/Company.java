package com.sinloans.loans.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;

@Table(name = "Company")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="full_company_name")
    private String fullName;

    @Column(name="short_company_name")
    private String shortName;

    @Column(name="tin")
    @Pattern(regexp = "\\d{10}")
    private String tin;

    @Column(name="iec")
    @Pattern(regexp = "\\d{9}")
    private String iec;

    @Column(name="legal_address")
    private String legalAddress;

    @Column(name="actual_address")
    private String actualAddress;

    @Column(name="psrn")
    @Pattern(regexp = "\\d{13}")
    private String psrn;

    @Column(name="okpo")
    @Pattern(regexp = "\\d{10}")
    private String okpo;

    @Column(name="okato")
    @Pattern(regexp = "\\d{10}")
    private String okato;
}
