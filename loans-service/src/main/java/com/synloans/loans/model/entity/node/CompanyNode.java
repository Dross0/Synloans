package com.synloans.loans.model.entity.node;

import com.synloans.loans.model.entity.company.Company;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "company_nodes")
@Getter
@Setter
public class CompanyNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "node_address")
    private String address;

    @Column(name = "node_user")
    private String user;

    @Column(name = "node_password")
    private String password;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
}
