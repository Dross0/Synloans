package com.synloans.loans.controller.company

import com.synloans.loans.mapper.CompanyMapper
import com.synloans.loans.model.dto.CompanyDto
import com.synloans.loans.model.entity.company.Company
import com.synloans.loans.service.company.CompanyService
import com.synloans.loans.service.exception.notfound.CompanyNotFoundException
import spock.lang.Specification

class CompanyControllerTest extends Specification{
    private CompanyController companyController
    private CompanyService companyService
    private CompanyMapper companyMapper

    def setup(){
        companyService = Mock(CompanyService)
        companyMapper = Mock(CompanyMapper)
        companyController = new CompanyController(companyService, companyMapper)
    }

    def "Тест. Получение компании по id"(){
        given:
            def companyId = 13
            def company = new Company()

            def expectedResponse = Stub(CompanyDto)
        when:
            def response = companyController.getById(companyId)
        then:
            1 * companyService.getById(companyId) >> Optional.of(company)
            1 * companyMapper.mapFrom(company) >> expectedResponse

            expectedResponse == response
    }

    def "Тест. Компания по id не найдена"(){
        given:
            def companyId = 12
        when:
            companyController.getById(companyId)
        then:
            1 * companyService.getById(companyId) >> Optional.empty()
            thrown(CompanyNotFoundException)
    }

    def "Тест. Получение всех компаний"(){
        given:
            def company1 = new Company()
            def company2 = new Company()

            def companyDto1 = Stub(CompanyDto)
            def companyDto2 = Stub(CompanyDto)
        when:
            def response = companyController.getCompanies()
        then:
            1 * companyService.getAll() >> [company1, company2]
            1 * companyMapper.mapFrom(company1) >> companyDto1
            1 * companyMapper.mapFrom(company2) >> companyDto2

            response.containsAll([companyDto1, companyDto2])
            response.size() == 2
    }
}
