package com.synloans.loans.mapper.converter;

import com.synloans.loans.mapper.Mapper;
import com.synloans.loans.model.dto.CompanyDto;
import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.model.entity.company.Company;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BankToCompanyConverter implements Converter<Bank, CompanyDto> {
    private final Mapper<Company, CompanyDto> companyMapper;

    @Override
    public CompanyDto convert(Bank bank){
        CompanyDto companyDto = companyMapper.mapFrom(bank.getCompany());
        companyDto.setId(bank.getId());
        return companyDto;
    }
}
