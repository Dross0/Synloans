package com.synloans.loans.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "syndicate")
@Getter
@Setter
public class Syndicate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "request")
    private LoanRequest request;

    @OneToMany(mappedBy = "syndicate")
    @JsonIgnore
    private Set<SyndicateParticipant> participants;
}
