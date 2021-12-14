package com.synloans.loans.model.mapper;

import com.synloans.loans.model.dto.CompanyDto;
import com.synloans.loans.model.entity.company.Company;


public class CompanyMapper {
    public CompanyDto entityToDto(Company company){
        return CompanyDto.builder()
                .id(company.getId())
                .fullName(company.getFullName())
                .shortName(company.getShortName())
                .inn(company.getInn())
                .kpp(company.getKpp())
                .actualAddress(company.getActualAddress())
                .legalAddress(company.getLegalAddress())
                .build();
    }
}
