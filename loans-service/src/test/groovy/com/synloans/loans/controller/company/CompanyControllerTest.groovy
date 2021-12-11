package com.synloans.loans.controller.company

import com.synloans.loans.controller.company.CompanyController
import com.synloans.loans.model.entity.Company
import com.synloans.loans.service.company.CompanyService
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

class CompanyControllerTest extends Specification{
    private CompanyController companyController
    private CompanyService companyService

    def setup(){
        companyService = Mock(CompanyService)
        companyController = new CompanyController(companyService)
    }

    def "Тест. Получение компании по id"(){
        given:
            def companyId = 13
            def company = Stub(Company){
                it.id >> companyId
                it.inn >> "123"
                it.kpp >> "345"
                it.fullName >> "SberBank"
                it.shortName >> "Sber"
                it.actualAddress >> "Act Address"
                it.legalAddress >> "Leg Address"
            }
        when:
            def dto = companyController.getById(companyId)
        then:
            1 * companyService.getById(companyId) >> Optional.of(company)
            with(dto){
                id == companyId
                inn == company.inn
                kpp == company.kpp
                fullName == company.fullName
                shortName == company.shortName
                actualAddress == company.actualAddress
                legalAddress == company.legalAddress
            }
    }

    def "Тест. Компания по id не найдена"(){
        given:
            def companyId = 12
        when:
            def dto = companyController.getById(companyId)
        then:
            1 * companyService.getById(companyId) >> Optional.empty()
            def e = thrown(ResponseStatusException)
            e.status == HttpStatus.NOT_FOUND
    }

    def "Тест. Получение всех компаний"(){
        given:
            def company = Stub(Company){
                it.id >> 1
                it.inn >> "123"
                it.kpp >> "345"
                it.fullName >> "SberBank"
                it.shortName >> "Sber"
                it.actualAddress >> "Act Address"
                it.legalAddress >> "Leg Address"
            }
            def company1 = Stub(Company){
                it.id >> 2
                it.inn >> "5555"
                it.kpp >> "333"
                it.fullName >> "SberBank1"
                it.shortName >> "Sber1"
                it.actualAddress >> "Act Address1"
                it.legalAddress >> "Leg Address1"
            }
        when:
            def res = companyController.getCompanies()
        then:
            1 * companyService.getAll() >> [company, company1]
            with(res[0]){
                id == company.id
                inn == company.inn
                kpp == company.kpp
                fullName == company.fullName
                shortName == company.shortName
                actualAddress == company.actualAddress
                legalAddress == company.legalAddress
            }
            with(res[1]){
                id == company1.id
                inn == company1.inn
                kpp == company1.kpp
                fullName == company1.fullName
                shortName == company1.shortName
                actualAddress == company1.actualAddress
                legalAddress == company1.legalAddress
            }
    }
}
