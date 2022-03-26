package com.synloans.loans.mapper

import com.synloans.loans.model.dto.CompanyDto
import com.synloans.loans.model.entity.company.Company
import spock.lang.Specification

class CompanyMapperTest extends Specification {
    private CompanyMapper companyMapper;

    def setup(){
        companyMapper = new CompanyMapper()
    }

    def "Тест. Компания из entity в dto"(){
        given:
            Company company = new Company();
            company.setId(1)
            company.setFullName("fullName")
            company.setShortName("shortName")
            company.setInn("123")
            company.setKpp("234")
            company.setActualAddress("actualAddress")
            company.setLegalAddress("legalAddress")
        when:
            def companyDto = companyMapper.mapFrom(company)
        then:
            verifyAll(companyDto){
                id == company.id
                fullName == company.fullName
                shortName == company.shortName
                inn == company.inn
                kpp == company.kpp
                actualAddress == company.actualAddress
                legalAddress == company.legalAddress
            }
    }

    def "Тест. Компания из dto в entity"(){
        given:
            CompanyDto companyDto = CompanyDto.builder()
                    .id(1)
                    .fullName("fullName")
                    .shortName("shortName")
                    .inn("123")
                    .kpp("234")
                    .actualAddress("actualAddress")
                    .legalAddress("legalAddress")
                    .build()
        when:
            def company = companyMapper.mapTo(companyDto)
        then:
            verifyAll(company){
                id == companyDto.id
                fullName == companyDto.fullName
                shortName == companyDto.shortName
                inn == companyDto.inn
                kpp == companyDto.kpp
                actualAddress == companyDto.actualAddress
                legalAddress == companyDto.legalAddress
            }
    }
}
