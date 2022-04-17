package com.synloans.loans.controller.loan


import com.synloans.loans.mapper.LoanRequestCollectionConverter
import com.synloans.loans.mapper.converter.LoanRequestConverter
import com.synloans.loans.model.dto.collection.LoanRequestCollection
import com.synloans.loans.model.dto.collection.LoanRequestCollectionResponse
import com.synloans.loans.model.dto.loanrequest.LoanRequestDto
import com.synloans.loans.model.dto.loanrequest.LoanRequestResponse
import com.synloans.loans.model.entity.company.Company
import com.synloans.loans.model.entity.loan.LoanRequest
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant
import com.synloans.loans.model.entity.user.User
import com.synloans.loans.security.UserRole
import com.synloans.loans.service.exception.UserUnauthorizedException
import com.synloans.loans.service.exception.notfound.LoanRequestNotFoundException
import com.synloans.loans.service.loan.LoanRequestService
import com.synloans.loans.service.syndicate.SyndicateParticipantService
import com.synloans.loans.service.user.UserService
import org.springframework.security.core.Authentication
import spock.lang.Specification

class LoanRequestControllerTest extends Specification{
    private LoanRequestController loanRequestController
    private LoanRequestService loanRequestService
    private SyndicateParticipantService syndicateParticipantService
    private LoanRequestConverter loanRequestConverter
    private LoanRequestCollectionConverter loanRequestCollectionConverter;
    private UserService userService

    def setup(){
        loanRequestService = Mock(LoanRequestService)
        userService = Mock(UserService)
        syndicateParticipantService = Mock(SyndicateParticipantService)
        loanRequestConverter = Mock(LoanRequestConverter)
        loanRequestCollectionConverter = Mock(LoanRequestCollectionConverter)
        loanRequestController = new LoanRequestController(
                loanRequestService,
                userService,
                syndicateParticipantService,
                loanRequestConverter,
                loanRequestCollectionConverter
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
            def auth = Stub(Authentication)
            def loanRqDto = new LoanRequestDto()

            def company = new Company()
            def loanRq = new LoanRequest()
            def user = new User()
            user.setCompany(company)

            def expectedResponse = new LoanRequestResponse()
        when:
            def response = loanRequestController.createLoanRequest(loanRqDto, auth)

        then:
            1 * userService.getCurrentUser(auth) >> user
            1 * loanRequestService.createRequest(loanRqDto, user.company) >> loanRq
            1 * loanRequestConverter.convert(loanRq) >> expectedResponse

            response == expectedResponse
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
            def auth = Stub(Authentication)
            def company = new Company()
            def loanRq = new LoanRequest()
            def user = Mock(User){
                it.company >> company
            }

            def expectedResponse = new LoanRequestResponse()
        when:
            def response = loanRequestController.getRequestById(loanRqId, auth)
        then:
            2 * userService.getCurrentUser(auth) >> user
            1 * loanRequestService.getOwnedCompanyLoanRequestById(loanRqId, user.company) >> loanRq
            1 * user.hasRole(UserRole.ROLE_BANK) >> false
            1 * loanRequestConverter.convert(loanRq) >> expectedResponse
            expectedResponse == response

    }

    def  "Тест. Получение банком заявки по id"(){
        given:
            def loanRqId = 11
            def auth = Stub(Authentication)
            def loanRq = new LoanRequest()
            def user = Mock(User)

            def expectedResponse = new LoanRequestResponse()
        when:
            def response = loanRequestController.getRequestById(loanRqId, auth)
        then:
            1 * userService.getCurrentUser(auth) >> user
            1 * user.hasRole(UserRole.ROLE_BANK) >> true
            1 * loanRequestConverter.convert(loanRq) >> expectedResponse
            1 * loanRequestService.getById(loanRqId) >> Optional.of(loanRq)
            expectedResponse == response
    }

    def "Тест. Не найдена заявка при получении банком заявки по id"(){
        given:
            def loanRqId = 11
            def auth = Stub(Authentication)
            def loanRq = new LoanRequest()
            def user = Mock(User)

        when:
            loanRequestController.getRequestById(loanRqId, auth)
        then:
            1 * userService.getCurrentUser(auth) >> user
            1 * user.hasRole(UserRole.ROLE_BANK) >> true
            0 * loanRequestConverter.convert(loanRq)
            1 * loanRequestService.getById(loanRqId) >> Optional.empty()

            thrown(LoanRequestNotFoundException)
    }


    def  "Тест. Получение всех заявок пользователя"(){
        given:
            def auth = Stub(Authentication)
            def loanRq1 = new LoanRequest()
            def loanRq2 = new LoanRequest()
            def company = Mock(Company)

            def user = Stub(User){
                it.company >> company
            }

            def loanResponse1 = new LoanRequestResponse()
            def loanResponse2 = new LoanRequestResponse()
        when:
            def response = loanRequestController.getCompanyRequests(auth)
        then:
            1 * userService.getCurrentUser(auth) >> user
            1 * company.getLoanRequests() >> [loanRq1, loanRq2]
            1 * loanRequestConverter.convert(loanRq1) >> loanResponse1
            1 * loanRequestConverter.convert(loanRq2) >> loanResponse2
            response == [loanResponse1, loanResponse2]

    }

    def  "Тест. Получение всех заявок"(){
        given:
            def auth = Stub(Authentication)

            def company = new Company()

            LoanRequestCollection requestCollection = new LoanRequestCollection()

            def user = new User()
            user.company = company

            def expectedResponse = new LoanRequestCollectionResponse()

        when:
            def response = loanRequestController.getAllRequests(auth)
        then:
            1 * userService.getCurrentUser(auth) >> user
            1 * loanRequestService.getAll(company) >> requestCollection
            1 * loanRequestCollectionConverter.convert(requestCollection) >> expectedResponse
            expectedResponse == response

    }

    def "Тест. Удаление заявки по id"(){
        given:
            def loanRqId = 11
            def auth = Stub(Authentication)
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
