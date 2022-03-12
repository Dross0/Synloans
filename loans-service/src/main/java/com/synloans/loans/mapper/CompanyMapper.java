package com.synloans.loans.mapper;

import com.synloans.loans.model.dto.CompanyDto;
import com.synloans.loans.model.entity.company.Company;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper implements Mapper<Company, CompanyDto> {
    @Override
    public CompanyDto mapFrom(Company company){
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

    @Override
    public Company mapTo(CompanyDto value) {
        Company company = new Company();
        company.setId(value.getId());
        company.setFullName(value.getFullName());
        company.setShortName(value.getShortName());
        company.setInn(value.getInn());
        company.setKpp(value.getKpp());
        company.setActualAddress(value.getActualAddress());
        company.setLegalAddress(value.getLegalAddress());
        return company;
    }

}
