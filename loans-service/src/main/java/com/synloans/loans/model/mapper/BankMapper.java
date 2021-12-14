package com.synloans.loans.model.mapper;

import com.synloans.loans.model.dto.CompanyDto;
import com.synloans.loans.model.entity.company.Bank;

public class BankMapper {
    private final CompanyMapper companyMapper = new CompanyMapper();

    public CompanyDto bankToDto(Bank bank){
        CompanyDto companyDto = companyMapper.entityToDto(bank.getCompany());
        companyDto.setId(bank.getId());
        return companyDto;
    }
}
