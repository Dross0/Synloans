package com.sinloans.loans.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class CompanyDto {
    private Long id;

    private String fullName;

    private String shortName;

    private String inn;

    private String kpp;

    private String legalAddress;

    private String actualAddress;
}
