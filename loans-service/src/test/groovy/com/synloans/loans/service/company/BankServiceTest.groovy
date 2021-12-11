package com.synloans.loans.service.company

import com.synloans.loans.model.entity.Bank
import com.synloans.loans.model.entity.Company
import com.synloans.loans.model.entity.Document
import com.synloans.loans.repositories.BankRepository
import com.synloans.loans.service.company.BankService
import spock.lang.Specification

class BankServiceTest extends Specification{
    private BankService bankService
    private BankRepository bankRepository

    def setup(){
        bankRepository = Mock(BankRepository)
        bankService = new BankService(bankRepository)
    }

    def "Тест. Создание банка по копмании и лицензии"(){
        given:
            def company = Stub(Company)
            def license = Stub(Document)
        when:
            def newBank = bankService.createBank(company, license)
        then:
            newBank.company == company
            newBank.license == license
            1 * bankRepository.save(_ as Bank) >> {Bank bank -> bank}
    }

    def "Тест. Создание банка по копмании"(){
        given:
            def company = Stub(Company)
        when:
            def newBank = bankService.createBank(company)
        then:
            newBank.company == company
            newBank.license == null
            1 * bankRepository.save(_ as Bank) >> {Bank bank -> bank}
    }

    def "Тест. Сохранение банка"(){
        given:
            def bank = Stub(Bank)
        when:
            def savedBank = bankService.save(bank)
        then:
            savedBank == bank
            1 * bankRepository.save(bank) >> bank
    }

    def "Тест. Получение всех банков"(){
        given:
            def banks = [Stub(Bank), Stub(Bank), Stub(Bank)]
        when:
            def allBanks = bankService.getAll()
        then:
            allBanks == banks
            1 * bankRepository.findAll() >> banks
    }

    def "Тест. Получение банка по id"(){
        when:
            def bank = bankService.getById(id)
        then:
            bank == resultBank
            1 * bankRepository.findById(id) >> Optional.ofNullable(resultBank)
        where:
            id  || resultBank
            2   || null
            10  || Stub(Bank)
    }

    def "Тест. Получение банка по компании"(){
        when:
            def foundBank = bankService.getByCompany(company)
        then:
            foundBank == bank
            1 * bankRepository.findByCompany(company) >> bank
        where:
            company       || bank
            null          || null
            Stub(Company) || Stub(Bank)
    }
}
