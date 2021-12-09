package com.synloans.loans.model.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
public class Profile {
    private String fullName;

    private String shortName;

    private String inn;

    private String kpp;

    private String legalAddress;

    private String actualAddress;

    private String email;

    private boolean creditOrganisation;
}
