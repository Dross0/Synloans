package com.synloans.loans.model.entity.company;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.synloans.loans.model.entity.loan.LoanRequest;
import com.synloans.loans.model.entity.node.CompanyNode;
import com.synloans.loans.model.entity.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Table(name = "Company")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="full_company_name")
    private String fullName;

    @Column(name="short_company_name")
    private String shortName;

    @Column(name="inn")
    @Pattern(regexp = "\\d{10}")
    private String inn;

    @Column(name="kpp")
    @Pattern(regexp = "\\d{9}")
    private String kpp;

    @Column(name="legal_address")
    private String legalAddress;

    @Column(name="actual_address")
    private String actualAddress;

    @Column(name="ogrn")
    @Pattern(regexp = "\\d{13}")
    private String ogrn;

    @Column(name="okpo")
    @Pattern(regexp = "\\d{10}")
    private String okpo;

    @Column(name="okato")
    @Pattern(regexp = "\\d{10}")
    private String okato;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<User> users;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<LoanRequest> loanRequests;

    @OneToMany(
            mappedBy = "company",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    private List<CompanyNode> nodes;

    public void addNode(CompanyNode node){
        Objects.requireNonNull(node, "Node cant be null");
        nodes.add(node);
        node.setCompany(this);
    }

    public void removeNode(CompanyNode node){
        Objects.requireNonNull(node, "Node cant be null");
        nodes.remove(node);
        node.setCompany(null);
    }
}
