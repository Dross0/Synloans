package com.sinloans.loans.model.mapper;

import com.sinloans.loans.model.dto.CompanyDto;
import com.sinloans.loans.model.entity.Company;


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
