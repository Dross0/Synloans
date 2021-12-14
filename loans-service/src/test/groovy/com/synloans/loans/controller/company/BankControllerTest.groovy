package com.synloans.loans.controller.company


import com.synloans.loans.model.entity.company.Bank
import com.synloans.loans.model.entity.company.Company
import com.synloans.loans.service.company.BankService
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

class BankControllerTest extends Specification{
    private BankController bankController
    private BankService bankService

    def setup(){
        bankService = Mock(BankService)
        bankController = new BankController(bankService)
    }

    def "Тест. Получение банка по id"(){
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
            def bankId = 12
            def bank = Stub(Bank){
                it.id >> bankId
                it.company >> company
            }
        when:
            def dto = bankController.getBankById(bankId)
        then:
            1 * bankService.getById(bankId) >> bank
            with(dto){
                id == bankId
                inn == company.inn
                kpp == company.kpp
                fullName == company.fullName
                shortName == company.shortName
                actualAddress == company.actualAddress
                legalAddress == company.legalAddress
            }
    }

    def "Тест. Банк по id не найден"(){
        given:
            def bankId = 12
        when:
            def dto = bankController.getBankById(bankId)
        then:
            1 * bankService.getById(bankId) >> null
            def e = thrown(ResponseStatusException)
            e.status == HttpStatus.NOT_FOUND
    }

    def "Тест. Получение всех банков"(){
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
            def bankId = 11
            def bankId1 = 22
            def bank = Stub(Bank){
                it.id >> bankId
                it.company >> company
            }
            def bank1 = Stub(Bank){
                it.id >> bankId1
                it.company >> company1
            }
        when:
            def res = bankController.getAllBanks()
        then:
            1 * bankService.getAll() >> [bank, bank1]
            with(res[0]){
                id == bankId
                inn == company.inn
                kpp == company.kpp
                fullName == company.fullName
                shortName == company.shortName
                actualAddress == company.actualAddress
                legalAddress == company.legalAddress
            }
            with(res[1]){
                id == bankId1
                inn == company1.inn
                kpp == company1.kpp
                fullName == company1.fullName
                shortName == company1.shortName
                actualAddress == company1.actualAddress
                legalAddress == company1.legalAddress
            }
    }
}
