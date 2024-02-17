package com.synloans.loans.model.dto.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class CompanyValidation {

    private String fullName;

    private String shortName;

    private String inn;

    private String kpp;

    private String legalAddress;

    private String actualAddress;

    private String ogrn;

    private String okpo;

    private String okato;
}
