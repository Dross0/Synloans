package com.synloans.loans.model.authentication;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class RegistrationRequest {
    private String fullName;
    private String shortName;
    private String inn;
    private String kpp;
    private String legalAddress;
    private String actualAddress;
    private boolean creditOrganisation;

    private String email;
    private String password;
}
