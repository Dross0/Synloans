package com.sinloans.loans.model.mapper;

import com.sinloans.loans.model.dto.CompanyDto;
import com.sinloans.loans.model.entity.Bank;

public class BankMapper {
    private final CompanyMapper companyMapper = new CompanyMapper();

    public CompanyDto bankToDto(Bank bank){
        CompanyDto companyDto = companyMapper.entityToDto(bank.getCompany());
        companyDto.setId(bank.getId());
        return companyDto;
    }
}
