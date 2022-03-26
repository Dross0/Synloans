package com.synloans.loans.controller.loan

import com.synloans.loans.mapper.CompanyMapper
import com.synloans.loans.mapper.converter.LoanRequestConverter
import com.synloans.loans.mapper.converter.SyndicateParticipantConverter
import com.synloans.loans.model.dto.LoanSum
import com.synloans.loans.model.dto.loanrequest.LoanRequestDto
import com.synloans.loans.model.dto.loanrequest.LoanRequestStatus
import com.synloans.loans.model.entity.company.Company
import com.synloans.loans.model.entity.loan.Loan
import com.synloans.loans.model.entity.loan.LoanRequest
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant
import com.synloans.loans.model.entity.user.User
import com.synloans.loans.service.exception.LoanRequestNotFoundException
import com.synloans.loans.service.exception.UserUnauthorizedException
import com.synloans.loans.service.loan.LoanRequestService
import com.synloans.loans.service.syndicate.SyndicateParticipantService
import com.synloans.loans.service.user.UserService
import org.springframework.security.core.Authentication
import spock.lang.Specification

import java.time.LocalDate

class LoanRequestControllerTest extends Specification{
    private LoanRequestController loanRequestController
    private LoanRequestService loanRequestService
    private SyndicateParticipantService syndicateParticipantService
    private UserService userService

    def setup(){
        loanRequestService = Mock(LoanRequestService)
        userService = Mock(UserService)
        syndicateParticipantService = Mock(SyndicateParticipantService)
        loanRequestController = new LoanRequestController(
                loanRequestService,
                userService,
                syndicateParticipantService,
                new SyndicateParticipantConverter(),
                new LoanRequestConverter(loanRequestService),
                new CompanyMapper()
        )
    }

    def "Тест. Ошибка в получении текущего пользователя"(){
        given:
            def username = "dross"
            def auth = Stub(Authentication)
            auth.getName() >> username
        when:
            loanRequestController.createLoanRequest(Stub(LoanRequestDto), auth)
        then:
            1 * userService.getCurrentUser(auth) >> {throw new UserUnauthorizedException()}
            thrown(UserUnauthorizedException)
    }

    def "Тест. Создание заявки"(){
        given:
            def loanRqId = 11
            def username = "dross"
            def auth = Stub(Authentication)
            auth.getName() >> username
            def loanRqDto = Stub(LoanRequestDto)

        def company = Stub(Company){
                it.id >> 10
                it.inn >> "123"
                it.kpp >> "345"
                it.fullName >> "SberBank"
                it.shortName >> "Sber"
                it.actualAddress >> "Act Address"
                it.legalAddress >> "Leg Address"
            }
            def loanRq = Stub(LoanRequest){
                it.company >> company
                it.id >> loanRqId
                it.term >> 14
                it.createDate >> LocalDate.now()
                it.rate >> 10.2d
                it.sum >> 100_000
                it.syndicate >> null
            }
            def user = Stub(User){
                it.company >> company
            }
        when:
            def response = loanRequestController.createLoanRequest(loanRqDto, auth)

        then:
            1 * userService.getCurrentUser(auth) >> user
            1 * loanRequestService.createRequest(loanRqDto, user.company) >> loanRq
            1 * loanRequestService.getStatus(loanRq) >> LoanRequestStatus.OPEN
            with(response){
                with(borrower){
                    id == company.id
                    inn == company.inn
                    kpp == company.kpp
                    fullName == company.fullName
                    shortName == company.shortName
                    actualAddress == company.actualAddress
                    legalAddress == company.legalAddress
                }
                banks.isEmpty()
                with(info){
                    status == LoanRequestStatus.OPEN
                    id == loanRq.id
                    term == loanRq.term
                    maxRate == loanRq.rate
                    dateIssue == null
                    dateCreate == loanRq.createDate
                    sum == LoanSum.valueOf(loanRq.sum)
                }
            }
    }

    def "Тест. Получение участников синдиката под заявкой"(){
        given:
            def loanRqId = 11
            def participants = [Stub(SyndicateParticipant), Stub(SyndicateParticipant), Stub(SyndicateParticipant)]
        when:
            def result = loanRequestController.getSyndicateParticipantsForRequest(loanRqId)
        then:
            1 * syndicateParticipantService.getSyndicateParticipantsByRequestId(loanRqId) >> participants
            result == participants
    }

    def  "Тест. Получение личной заявки по id"(){
        given:
            def loanRqId = 11
            def username = "dross"
            def auth = Stub(Authentication)
            auth.getName() >> username
            def company = Stub(Company){
                it.id >> 10
                it.inn >> "123"
                it.kpp >> "345"
                it.fullName >> "SberBank"
                it.shortName >> "Sber"
                it.actualAddress >> "Act Address"
                it.legalAddress >> "Leg Address"
            }
            def loanRq = Stub(LoanRequest){
                it.company >> company
                it.id >> loanRqId
                it.term >> 14
                it.createDate >> LocalDate.now()
                it.rate >> 10.2d
                it.sum >> 100_000
                it.syndicate >> null
            }
            def user = Stub(User){
                it.company >> company
            }
        when:
            def response = loanRequestController.getRequestById(loanRqId, auth)
        then:
            2 * userService.getCurrentUser(auth) >> user
            1 * loanRequestService.getOwnedCompanyLoanRequestById(loanRqId, user.company) >> loanRq
            1 * loanRequestService.getStatus(loanRq) >> LoanRequestStatus.OPEN
            with(response){
                with(borrower){
                    id == company.id
                    inn == company.inn
                    kpp == company.kpp
                    fullName == company.fullName
                    shortName == company.shortName
                    actualAddress == company.actualAddress
                    legalAddress == company.legalAddress
                }
                banks.isEmpty()
                with(info){
                    status == LoanRequestStatus.OPEN
                    id == loanRq.id
                    term == loanRq.term
                    maxRate == loanRq.rate
                    dateIssue == null
                    dateCreate == loanRq.createDate
                    sum == LoanSum.valueOf(loanRq.sum)
                }
            }

    }

    def  "Тест. Получение банком заявки по id"(){
        given:
            def loanRqId = 11
            def username = "dross"
            def auth = Stub(Authentication)
            auth.getName() >> username
            def company = Stub(Company){
                it.id >> 10
                it.inn >> "123"
                it.kpp >> "345"
                it.fullName >> "SberBank"
                it.shortName >> "Sber"
                it.actualAddress >> "Act Address"
                it.legalAddress >> "Leg Address"
            }
            def loanRq = Stub(LoanRequest){
                it.company >> company
                it.id >> loanRqId
                it.term >> 14
                it.createDate >> LocalDate.now()
                it.rate >> 10.2d
                it.sum >> 100_000
                it.syndicate >> null
            }
            def user = Mock(User){
                it.company >> Stub(Company)
            }
        when:
            def response = loanRequestController.getRequestById(loanRqId, auth)
        then:
            1 * userService.getCurrentUser(auth) >> user
            1 * user.hasRole("ROLE_BANK") >> true
            1 * loanRequestService.getById(loanRq.id) >> Optional.of(loanRq)
            1 * loanRequestService.getStatus(loanRq) >> LoanRequestStatus.OPEN

            with(response){
                with(borrower){
                    id == company.id
                    inn == company.inn
                    kpp == company.kpp
                    fullName == company.fullName
                    shortName == company.shortName
                    actualAddress == company.actualAddress
                    legalAddress == company.legalAddress
                }
                banks.isEmpty()
                with(info){
                    status == LoanRequestStatus.OPEN
                    id == loanRq.id
                    term == loanRq.term
                    maxRate == loanRq.rate
                    dateIssue == null
                    dateCreate == loanRq.createDate
                    sum == LoanSum.valueOf(loanRq.sum)
                }
            }
    }

    def "Тест. Не найдена заявка при получении банком заявки по id"(){
        given:
            def loanRqId = 11
            def username = "dross"
            def auth = Stub(Authentication)
            auth.getName() >> username
            def loanRq = Stub(LoanRequest){
                it.id >> loanRqId
            }
            def user = Mock(User){
                it.company >> Stub(Company)
            }
        when:
            def response = loanRequestController.getRequestById(loanRqId, auth)
        then:
            1 * userService.getCurrentUser(auth) >> user
            1 * user.hasRole("ROLE_BANK") >> true
            1 * loanRequestService.getById(loanRq.id) >> Optional.empty()
            thrown(LoanRequestNotFoundException)
    }

    def  "Тест. Получение всех заявок пользователя"(){
        given:
            def username = "dross"
            def auth = Stub(Authentication)
            auth.getName() >> username
            def loanRq1 = Stub(LoanRequest){
                it.id >> 1
                it.term >> 14
                it.createDate >> LocalDate.now()
                it.rate >> 10.2d
                it.sum >> 100_000
                it.syndicate >> null
            }
            def loanRq2 = Stub(LoanRequest){
                it.id >> 2
                it.term >> 11
                it.createDate >> LocalDate.now()
                it.rate >> 105.2d
                it.sum >> 1_000
                it.syndicate >> null
                it.loan >> Stub(Loan){
                    registrationDate >> LocalDate.now()
                }
            }
            def company = Stub(Company){
                it.id >> 10
                it.inn >> "123"
                it.kpp >> "345"
                it.fullName >> "SberBank"
                it.shortName >> "Sber"
                it.actualAddress >> "Act Address"
                it.legalAddress >> "Leg Address"
                it.loanRequests >> [loanRq1, loanRq2]
            }
            loanRq1.company >> company
            loanRq2.company >> company

            def user = Stub(User){
                it.company >> company
            }
        when:
            def response = loanRequestController.getCompanyRequests(auth)
        then:
            1 * userService.getCurrentUser(auth) >> user
            1 * loanRequestService.getStatus(loanRq1) >> LoanRequestStatus.OPEN
            1 * loanRequestService.getStatus(loanRq2) >> LoanRequestStatus.ISSUE
            with(response[0]){
                with(borrower){
                    id == company.id
                    inn == company.inn
                    kpp == company.kpp
                    fullName == company.fullName
                    shortName == company.shortName
                    actualAddress == company.actualAddress
                    legalAddress == company.legalAddress
                }
                banks.isEmpty()
                with(info){
                    status == LoanRequestStatus.OPEN
                    id == loanRq1.id
                    term == loanRq1.term
                    maxRate == loanRq1.rate
                    dateIssue == null
                    dateCreate == loanRq1.createDate
                    sum == LoanSum.valueOf(loanRq1.sum)
                }
            }
            with(response[1]){
                with(borrower){
                    id == company.id
                    inn == company.inn
                    kpp == company.kpp
                    fullName == company.fullName
                    shortName == company.shortName
                    actualAddress == company.actualAddress
                    legalAddress == company.legalAddress
                }
                banks.isEmpty()
                with(info){
                    status == LoanRequestStatus.ISSUE
                    id == loanRq2.id
                    term == loanRq2.term
                    maxRate == loanRq2.rate
                    dateIssue == loanRq2.loan.registrationDate
                    dateCreate == loanRq2.createDate
                    sum == LoanSum.valueOf(loanRq2.sum)
                }
            }
    }

    def  "Тест. Получение всех заявок"(){
        given:
            def company = Stub(Company){
                it.id >> 10
                it.inn >> "123"
                it.kpp >> "345"
                it.fullName >> "SberBank"
                it.shortName >> "Sber"
                it.actualAddress >> "Act Address"
                it.legalAddress >> "Leg Address"
            }
            def loanRq1 = Stub(LoanRequest){
                it.id >> 1
                it.term >> 14
                it.createDate >> LocalDate.now()
                it.rate >> 10.2d
                it.sum >> 100_000
                it.syndicate >> null
                it.company >> company
            }
            def loanRq2 = Stub(LoanRequest){
                it.id >> 2
                it.term >> 11
                it.createDate >> LocalDate.now()
                it.rate >> 105.2d
                it.sum >> 1_000
                it.syndicate >> null
                it.company >> company
                it.loan >> Stub(Loan){
                    registrationDate >> LocalDate.now()
                }
            }

        when:
            def response = loanRequestController.getAllRequests()
        then:
            1 * loanRequestService.getAll() >> [loanRq1, loanRq2]
            1 * loanRequestService.getStatus(loanRq1) >> LoanRequestStatus.OPEN
            1 * loanRequestService.getStatus(loanRq2) >> LoanRequestStatus.ISSUE
            with(response[0]){
                with(borrower){
                    id == company.id
                    inn == company.inn
                    kpp == company.kpp
                    fullName == company.fullName
                    shortName == company.shortName
                    actualAddress == company.actualAddress
                    legalAddress == company.legalAddress
                }
                banks.isEmpty()
                with(info){
                    status == LoanRequestStatus.OPEN
                    id == loanRq1.id
                    term == loanRq1.term
                    maxRate == loanRq1.rate
                    dateIssue == null
                    dateCreate == loanRq1.createDate
                    sum == LoanSum.valueOf(loanRq1.sum)
                }
            }
            with(response[1]){
                with(borrower){
                    id == company.id
                    inn == company.inn
                    kpp == company.kpp
                    fullName == company.fullName
                    shortName == company.shortName
                    actualAddress == company.actualAddress
                    legalAddress == company.legalAddress
                }
                banks.isEmpty()
                with(info){
                    status == LoanRequestStatus.ISSUE
                    id == loanRq2.id
                    term == loanRq2.term
                    maxRate == loanRq2.rate
                    dateIssue == loanRq2.loan.registrationDate
                    dateCreate == loanRq2.createDate
                    sum == LoanSum.valueOf(loanRq2.sum)
                }
            }
    }

    def "Тест. Удаление заявки по id"(){
        given:
            def loanRqId = 11
            def username = "dross"
            def auth = Stub(Authentication)
            auth.getName() >> username
            def company = Stub(Company){
                it.id >> 10
            }
            def loanRq = Stub(LoanRequest){
                it.company >> company
            }
            def user = Stub(User){
                it.company >> company
            }
        when:
            loanRequestController.deleteRequest(loanRqId, auth)
        then:
            1 * userService.getCurrentUser(auth) >> user
            1 * loanRequestService.getOwnedCompanyLoanRequestById(loanRqId, user.company) >> loanRq
            1 * loanRequestService.deleteById(loanRqId)
    }
}
