package com.synloans.loans.mapper.converter


import com.synloans.loans.model.dto.LoanSum
import com.synloans.loans.model.dto.loanrequest.LoanRequestInfo
import com.synloans.loans.model.dto.loanrequest.LoanRequestStatus
import com.synloans.loans.model.entity.loan.Loan
import com.synloans.loans.model.entity.loan.LoanRequest
import com.synloans.loans.service.loan.LoanRequestService
import spock.lang.Specification

import java.time.LocalDate

class LoanRequestConverterTest extends Specification{

    private LoanRequestConverter loanRequestConverter
    private LoanRequestService loanRequestService

    def setup(){
        loanRequestService = Mock(LoanRequestService)
        loanRequestConverter = new LoanRequestConverter(loanRequestService)
    }

    def "Тест. Заявка на кредит из entity в dto. До кредита"(){
        given:
            LoanRequest loanRequest = new LoanRequest()
            loanRequest.id = 1
            loanRequest.term = 10
            loanRequest.rate = 10.2
            loanRequest.createDate = LocalDate.now()
            loanRequest.sum = 1000
            loanRequest.loan = null

            LoanRequestStatus expectedStatus = LoanRequestStatus.OPEN

        when:
            LoanRequestInfo loanRequestInfo = loanRequestConverter.convert(loanRequest)
        then:
            1 * loanRequestService.getStatus(loanRequest) >> expectedStatus

            verifyAll(loanRequestInfo){
                id == loanRequest.id
                sum == LoanSum.valueOf(loanRequest.sum)
                Math.abs(maxRate - loanRequest.rate) < 0.000001d
                dateCreate == loanRequest.createDate
                dateIssue == null
                term == loanRequest.term
                status == expectedStatus
            }
    }

    def "Тест. Заявка на кредит из entity в dto. C кредитом"(){
        given:
            LoanRequest loanRequest = new LoanRequest()
            loanRequest.id = 1
            loanRequest.term = 10
            loanRequest.rate = 10.2
            loanRequest.createDate = LocalDate.now()
            loanRequest.sum = 1000

            Loan loan = new Loan()
            loan.registrationDate = LocalDate.now().plusMonths(1)
            loanRequest.loan = loan

            LoanRequestStatus expectedStatus = LoanRequestStatus.TRANSFER

        when:
            LoanRequestInfo loanRequestInfo = loanRequestConverter.convert(loanRequest)
        then:
            1 * loanRequestService.getStatus(loanRequest) >> expectedStatus

            verifyAll(loanRequestInfo){
                id == loanRequest.id
                sum == LoanSum.valueOf(loanRequest.sum)
                Math.abs(maxRate - loanRequest.rate) < 0.000001d
                dateCreate == loanRequest.createDate
                dateIssue == loan.registrationDate
                term == loanRequest.term
                status == expectedStatus
            }
    }
}
