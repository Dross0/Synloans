package com.synloans.loans.service.loan

import com.synloans.loans.model.dto.loanrequest.LoanRequestDto
import com.synloans.loans.model.dto.loanrequest.LoanRequestStatus
import com.synloans.loans.model.entity.company.Bank
import com.synloans.loans.model.entity.company.Company
import com.synloans.loans.model.entity.loan.Loan
import com.synloans.loans.model.entity.loan.LoanRequest
import com.synloans.loans.model.entity.syndicate.Syndicate
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant
import com.synloans.loans.repository.loan.LoanRequestRepository
import com.synloans.loans.service.exception.ForbiddenResourceException
import com.synloans.loans.service.exception.InvalidLoanRequestException
import com.synloans.loans.service.exception.notfound.LoanRequestNotFoundException
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
            SyndicateParticipant curr = buildParticipant()
            SyndicateParticipant otherParticipant1 = buildParticipant()
            SyndicateParticipant otherParticipant2 = buildParticipant()
            SyndicateParticipant otherParticipant3 = buildParticipant()
            SyndicateParticipant otherParticipant4 = buildParticipant()

            LoanRequest own1 = new LoanRequest()
            Syndicate ownSynd1 = new Syndicate()
            ownSynd1.participants = [curr, otherParticipant1, otherParticipant2, otherParticipant3, otherParticipant4]
            own1.syndicate = ownSynd1

            LoanRequest own2 = new LoanRequest()
            Syndicate ownSynd2 = new Syndicate()
            ownSynd2.participants = [curr]
            own2.syndicate = ownSynd2

            LoanRequest other1 = new LoanRequest()
            Syndicate otherSyndicate = new Syndicate()
            otherSyndicate.participants = [otherParticipant1, otherParticipant2, otherParticipant3, otherParticipant4]
            other1.syndicate = otherSyndicate

            LoanRequest other2 = new LoanRequest()
        when:
            def requests = loanRequestService.getAll(curr.getBank().getCompany())
        then:
            1 * loanRequestRepository.findAll() >> [own1, other1, own2, other2]
            requests.getOtherRequests().size() == 2
            requests.getOtherRequests().containsAll([other1, other2])

            requests.getOwnRequests().size() == 2
            requests.getOwnRequests().containsAll([own1, own2])

    }

    static def buildParticipant(){
        Company company = new Company()
        Bank bank = new Bank()
        bank.company = company
        SyndicateParticipant participant = new SyndicateParticipant()
        participant.bank = bank
        return participant
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
                sum == sumVal
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
            sumVal = 123_000_000
    }

    def "Тест. Получение статуса по заявке"(){
        given:
            def loan = new Loan()
            loan.closeDate = closeDate
            def loanRequest = new LoanRequest()
            if (closeDate != null) {
                loanRequest.loan = loan
            }
        when:
            def status = loanRequestService.getStatus(loanRequest)
        then:
            status == expectedStatus
        where:
            closeDate                        || expectedStatus
            null                             || LoanRequestStatus.OPEN
            LocalDate.of(2013, 1, 1)         || LoanRequestStatus.CLOSE
            LocalDate.now().plusMonths(1)    || LoanRequestStatus.ISSUE
    }

    def "Тест. Получение заявки компании по id"(){
        given:
            def loanRqId = 11
            def company = Stub(Company){
                id >> 1
            }
            def loanRq = Stub(LoanRequest){
                it.company >> company
            }
        when:
            def result = loanRequestService.getOwnedCompanyLoanRequestById(loanRqId, company)
        then:
            1 * loanRequestRepository.findById(loanRqId) >> Optional.of(loanRq)
            result == loanRq
    }

    def  "Тест. Заявка не найдена при получении заявки компании по id"(){
        given:
            def loanRqId = 11
            def company = Stub(Company)
        when:
            loanRequestService.getOwnedCompanyLoanRequestById(loanRqId, company)
        then:
            1 * loanRequestRepository.findById(loanRqId) >> Optional.empty()
            thrown(LoanRequestNotFoundException)
    }

    def  "Тест. Найденная заявка по id не принадлежит компании"(){
        given:
            def loanRqId = 11
            def company = Stub(Company){
                id >> 1
            }
            def loanRq = Stub(LoanRequest){
                it.company >> Stub(Company){
                    it.id >> 123
                }
            }
        when:
            loanRequestService.getOwnedCompanyLoanRequestById(loanRqId, company)
        then:
            1 * loanRequestRepository.findById(loanRqId) >> Optional.of(loanRq)
            thrown(ForbiddenResourceException)
    }

    def "Тест. Расчет доступной суммы собранной синдикатом по заявке"(){
        given:
            def participants = [
                    Stub(SyndicateParticipant){
                        loanSum >> 123_000
                    },
                    Stub(SyndicateParticipant){
                        loanSum >> 1_500_000
                    },
                    Stub(SyndicateParticipant){
                        loanSum >> 300_000
                    }
            ]
            def syndicate = Stub(Syndicate){
                it.participants >> participants
            }
            def loanRequest = Stub(LoanRequest){
                it.syndicate >> syndicate
            }
        when:
            def sum = loanRequestService.calcSumFromSyndicate(loanRequest)
        then:
            sum == 1_923_000
            noExceptionThrown()
    }

    def "Тест. Отсутсвует синдикат при расчете собранной суммы"(){
        given:
            def loanRequest = Stub(LoanRequest){
                syndicate >> null
            }
        when:
            def sum = loanRequestService.calcSumFromSyndicate(loanRequest)
        then:
            thrown(InvalidLoanRequestException)
    }
}
