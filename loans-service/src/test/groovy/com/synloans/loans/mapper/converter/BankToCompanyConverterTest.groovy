package com.synloans.loans.mapper.converter

import com.synloans.loans.mapper.CompanyMapper
import com.synloans.loans.model.dto.CompanyDto
import com.synloans.loans.model.entity.company.Bank
import com.synloans.loans.model.entity.company.Company
import spock.lang.Specification

class BankToCompanyConverterTest extends Specification{

    private BankToCompanyConverter bankToCompanyConverter
    private CompanyMapper companyMapper

    def setup(){
        companyMapper = Mock(CompanyMapper)
        bankToCompanyConverter = new BankToCompanyConverter(companyMapper)
    }

    def "Тест. Банк из entity в dto с описанием компании"(){
        given:
            Company company = new Company()
            Bank bank = new Bank()
            bank.company = company
            bank.id = 10

            CompanyDto expectedCompanyDto = CompanyDto.builder().build()
        when:
            CompanyDto companyDto = bankToCompanyConverter.convert(bank)

        then:
            1 * companyMapper.mapFrom(company) >> expectedCompanyDto
            companyDto == expectedCompanyDto
            companyDto.id == bank.id
    }
}
