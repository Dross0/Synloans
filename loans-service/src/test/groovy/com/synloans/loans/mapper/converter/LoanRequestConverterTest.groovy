package com.synloans.loans.mapper.converter

import com.synloans.loans.mapper.CompanyMapper
import com.synloans.loans.model.dto.BankParticipantInfo
import com.synloans.loans.model.dto.CompanyDto
import com.synloans.loans.model.dto.LoanSum
import com.synloans.loans.model.dto.loanrequest.LoanRequestResponse
import com.synloans.loans.model.dto.loanrequest.LoanRequestStatus
import com.synloans.loans.model.entity.company.Company
import com.synloans.loans.model.entity.loan.LoanRequest
import com.synloans.loans.model.entity.syndicate.Syndicate
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant
import com.synloans.loans.service.loan.LoanRequestService
import spock.lang.Specification

import java.time.LocalDate

class LoanRequestConverterTest extends Specification{

    private LoanRequestConverter loanRequestConverter
    private LoanRequestService loanRequestService
    private SyndicateParticipantConverter syndicateParticipantConverter
    private CompanyMapper companyMapper

    def setup(){
        loanRequestService = Mock(LoanRequestService)
        companyMapper = Mock(CompanyMapper)
        syndicateParticipantConverter = Mock(SyndicateParticipantConverter)
        loanRequestConverter = new LoanRequestConverter(
                loanRequestService,
                syndicateParticipantConverter,
                companyMapper
        )
    }

    def "Тест. Заявка на кредит из entity в dto. До кредита"(){
        given:
            Company borrower = new Company()

            SyndicateParticipant participant1 = new SyndicateParticipant()
            SyndicateParticipant participant2 = new SyndicateParticipant()

            Syndicate syndicate = new Syndicate()
            syndicate.setParticipants([participant1, participant2] as Set)

            BankParticipantInfo bankParticipantInfo1 = Stub(BankParticipantInfo)
            BankParticipantInfo bankParticipantInfo2 = Stub(BankParticipantInfo)

            LoanRequest loanRequest = new LoanRequest()
            loanRequest.id = 1
            loanRequest.term = 10
            loanRequest.rate = 10.2
            loanRequest.createDate = LocalDate.now()
            loanRequest.sum = 1000
            loanRequest.loan = null
            loanRequest.company = borrower
            loanRequest.syndicate = syndicate

            LoanRequestStatus expectedStatus = LoanRequestStatus.OPEN

            CompanyDto borrowerDto = Stub(CompanyDto)

        when:
            LoanRequestResponse response = loanRequestConverter.convert(loanRequest)
        then:
            1 * loanRequestService.getStatus(loanRequest) >> expectedStatus

            1 * companyMapper.mapFrom(borrower) >> borrowerDto

            1 * syndicateParticipantConverter.convert(participant1) >> bankParticipantInfo1
            1 * syndicateParticipantConverter.convert(participant2) >> bankParticipantInfo2

            response.banks.containsAll([bankParticipantInfo1, bankParticipantInfo2])

            response.getBorrower() == borrowerDto

            verifyAll(response.getInfo()){
                id == loanRequest.id
                sum == LoanSum.valueOf(loanRequest.sum)
                Math.abs(maxRate - loanRequest.rate) < 0.000001d
                dateCreate == loanRequest.createDate
                dateIssue == null
                term == loanRequest.term
                status == expectedStatus
            }
    }

    def "Тест. Заявка на кредит из entity в dto без синдиката"(){
        given:
            Company borrower = new Company()

            LoanRequest loanRequest = new LoanRequest()
            loanRequest.id = 1
            loanRequest.term = 10
            loanRequest.rate = 10.2
            loanRequest.createDate = LocalDate.now()
            loanRequest.sum = 1000
            loanRequest.loan = null
            loanRequest.company = borrower
            loanRequest.syndicate = null

            LoanRequestStatus expectedStatus = LoanRequestStatus.OPEN

            CompanyDto borrowerDto = Stub(CompanyDto)

        when:
            LoanRequestResponse response = loanRequestConverter.convert(loanRequest)
        then:
            1 * loanRequestService.getStatus(loanRequest) >> expectedStatus

            1 * companyMapper.mapFrom(borrower) >> borrowerDto

            0 * syndicateParticipantConverter.convert(_)

            response.banks.isEmpty()

            response.getBorrower() == borrowerDto

            verifyAll(response.getInfo()){
                id == loanRequest.id
                sum == LoanSum.valueOf(loanRequest.sum)
                Math.abs(maxRate - loanRequest.rate) < 0.000001d
                dateCreate == loanRequest.createDate
                dateIssue == null
                term == loanRequest.term
                status == expectedStatus
            }
    }
}
