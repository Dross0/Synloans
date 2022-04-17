package com.synloans.loans.controller.company


import com.synloans.loans.mapper.converter.BankToCompanyConverter
import com.synloans.loans.model.dto.CompanyDto
import com.synloans.loans.model.entity.company.Bank
import com.synloans.loans.service.company.BankService
import com.synloans.loans.service.exception.notfound.BankNotFoundException
import spock.lang.Specification

class BankControllerTest extends Specification{
    private BankController bankController
    private BankService bankService
    private BankToCompanyConverter bankToCompanyConverter

    def setup(){
        bankService = Mock(BankService)
        bankToCompanyConverter = Mock(BankToCompanyConverter)
        bankController = new BankController(bankService, bankToCompanyConverter)
    }

    def "Тест. Получение банка по id"(){
        given:
            def bankId = 12
            def bank = new Bank()

            def expectedResponse = Stub(CompanyDto)
        when:
            def response = bankController.getBankById(bankId)
        then:
            1 * bankService.getById(bankId) >> bank
            1 * bankToCompanyConverter.convert(bank) >> expectedResponse
            expectedResponse == response
    }

    def "Тест. Банк по id не найден"(){
        given:
            def bankId = 12
        when:
            bankController.getBankById(bankId)
        then:
            1 * bankService.getById(bankId) >> null
            thrown(BankNotFoundException)
    }

    def "Тест. Получение всех банков"(){
        given:
            def company1 = Stub(CompanyDto)
            def company2 = Stub(CompanyDto)
            def bank1 = new Bank()
            def bank2 = new Bank()
        when:
            def response = bankController.getAllBanks()
        then:
            1 * bankService.getAll() >> [bank1, bank2]
            1 * bankToCompanyConverter.convert(bank1) >> company1
            1 * bankToCompanyConverter.convert(bank2) >> company2

            response.size() == 2
            response.containsAll([company1, company2])


    }
}
