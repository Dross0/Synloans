package com.synloans.loans.service.company

import com.synloans.loans.model.entity.Company
import com.synloans.loans.repositories.CompanyRepository
import com.synloans.loans.service.company.CompanyService
import spock.lang.Specification

class CompanyServiceTest extends Specification{
    private CompanyService companyService
    private CompanyRepository companyRepository

    def setup(){
        companyRepository = Mock(CompanyRepository)
        companyService = new CompanyService(companyRepository)
    }

    def "Тест. Создание компании с существующими ИНН и КПП"(){
        given:
            def company = Stub(Company)
            company.inn = "123"
            company.kpp = "234"
        when:
            def createdCompany = companyService.create(company)
        then:
            createdCompany == null
            1 * companyRepository.existsByInnAndKpp(company.inn, company.kpp) >> true
            0 * companyRepository.save(_)
    }

    def "Тест. Создание компании"(){
        given:
            def company = Stub(Company)
            company.inn = "123"
            company.kpp = "234"
        when:
            def savedCompany = companyService.create(company)
        then:
            savedCompany == company
            1 * companyRepository.existsByInnAndKpp(company.inn, company.kpp) >> false
            1 * companyRepository.save(company) >> company
    }

    def "Тест. Сохранение компании"(){
        given:
            def company = Stub(Company)
        when:
            def savedCompany = companyService.save(company)
        then:
            savedCompany == company
            1 * companyRepository.save(company) >> company
    }

    def "Тест. Получение всех компаний"(){
        given:
            def companies = [Stub(Company), Stub(Company), Stub(Company)]
        when:
            def allCompanies = companyService.getAll()
        then:
            allCompanies == companies
            1 * companyRepository.findAll() >> companies
    }

    def "Тест. Получение компании по id"(){
        when:
            def company = companyService.getById(id)
        then:
            company == resultCompany
            1 * companyRepository.findById(id) >> resultCompany
        where:
            id  || resultCompany
            2   || Optional.empty()
            10  || Optional.of(Stub(Company))
    }

    def "Тест. Получение компании по инн и кпп"(){
        when:
            def foundCompany = companyService.getByInnAndKpp(inn, kpp)
        then:
            foundCompany.orElse(null) == company
            1 * companyRepository.findByInnAndKpp(inn, kpp) >> company
        where:
            inn  | kpp  || company
            "1"  | "2"  || null
            "3"  | "4"  || Stub(Company)
    }
}
