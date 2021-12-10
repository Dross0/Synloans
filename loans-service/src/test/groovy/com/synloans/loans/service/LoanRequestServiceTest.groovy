package com.synloans.loans.service

import com.synloans.loans.model.dto.LoanSum
import com.synloans.loans.model.dto.SumUnit
import com.synloans.loans.model.dto.loanrequest.LoanRequestDto
import com.synloans.loans.model.dto.loanrequest.LoanRequestStatus
import com.synloans.loans.model.entity.Company
import com.synloans.loans.model.entity.Loan
import com.synloans.loans.model.entity.LoanRequest
import com.synloans.loans.repositories.LoanRequestRepository
import spock.lang.Specification

import java.time.LocalDate

class LoanRequestServiceTest extends Specification {
    private LoanRequestService loanRequestService
    private LoanRequestRepository loanRequestRepository

    def setup(){
        loanRequestRepository = Mock(LoanRequestRepository)
        loanRequestService = new LoanRequestService(loanRequestRepository)
    }

    def "Тест. Сохранение заявки"(){
        given:
            def request = Stub(LoanRequest)
        when:
            def savedRequest = loanRequestService.save(request)
        then:
            savedRequest == request
            1 * loanRequestRepository.save(request) >> request
    }

    def "Тест. Получение всех заявок"(){
        given:
            def requests = [Stub(LoanRequest), Stub(LoanRequest), Stub(LoanRequest)]
        when:
            def allRequests = loanRequestService.getAll()
        then:
            allRequests == requests
            1 * loanRequestRepository.findAll() >> requests
    }

    def "Тест. Получение заявки по id"(){
        when:
            def request = loanRequestService.getById(id)
        then:
            request == resultRequest
            1 * loanRequestRepository.findById(id) >> resultRequest
        where:
            id  || resultRequest
            2   || Optional.empty()
            10  || Optional.of(Stub(LoanRequest))
    }

    def "Тест. Удаление заявки по id"(){
        when:
            loanRequestService.deleteById(id)
        then:
            1 * loanRequestRepository.deleteById(id)
        where:
            id = 1
    }

    def "Тест. Создание заявки по dto и компании"(){
        given:
            def loanRequestDto = Mock(LoanRequestDto)
            loanRequestDto.term >> termVal
            loanRequestDto.sum >> sumVal
            loanRequestDto.maxRate >> maxRateVal
            def companyArg = Stub(Company)
        when:
            def loanRequest = loanRequestService.createRequest(loanRequestDto, companyArg)
        then:
            1 * loanRequestRepository.save(_ as LoanRequest) >> {LoanRequest l -> l}
            with(loanRequest){
                sum == sumVal.getSum()
                term == termVal
                company == companyArg
                rate == maxRateVal
                loan == null
                syndicate == null
                createDate == LocalDate.now()
            }
        where:
            termVal = 10
            maxRateVal = 14.2d
            sumVal = new LoanSum(123, SumUnit.MILLION)
    }

    def "Тест. Получение статуса по заявке"(){
        given:
            def loanStub = Stub(Loan)
            loanStub.closeDate >> closeDate
            def loanRequest = Stub(LoanRequest)
            loanRequest.loan >> (closeDate == null ? null : loanStub)
        when:
            def status = loanRequestService.getStatus(loanRequest)
        then:
            status == expectedStatus
        where:
            closeDate                        || expectedStatus
            null                             || LoanRequestStatus.OPEN
            LocalDate.of(2013, 1, 1)         || LoanRequestStatus.CLOSE
            LocalDate.now().plusMonths(1)    || LoanRequestStatus.ISSUE
        //TODO LoanRequestStatus.TRANSFER
    }
}