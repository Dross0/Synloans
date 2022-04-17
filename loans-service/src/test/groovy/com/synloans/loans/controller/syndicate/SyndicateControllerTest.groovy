package com.synloans.loans.controller.syndicate


import com.synloans.loans.model.dto.SyndicateJoinRequest
import com.synloans.loans.model.entity.company.Bank
import com.synloans.loans.model.entity.company.Company
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant
import com.synloans.loans.model.entity.user.User
import com.synloans.loans.service.company.BankService
import com.synloans.loans.service.exception.SyndicateJoinException
import com.synloans.loans.service.exception.UserUnauthorizedException
import com.synloans.loans.service.exception.notfound.BankNotFoundException
import com.synloans.loans.service.syndicate.SyndicateParticipantService
import com.synloans.loans.service.syndicate.SyndicateService
import com.synloans.loans.service.user.UserService
import org.springframework.security.core.Authentication
import spock.lang.Specification

class SyndicateControllerTest extends Specification{
    private SyndicateController syndicateController
    private SyndicateService syndicateService
    private SyndicateParticipantService syndicateParticipantService
    private BankService bankService
    private UserService userService

    def setup(){
        syndicateService = Mock(SyndicateService)
        syndicateParticipantService = Mock(SyndicateParticipantService)
        bankService = Mock(BankService)
        userService = Mock(UserService)
        syndicateController = new SyndicateController(userService, bankService, syndicateService, syndicateParticipantService)
    }

    def "Тест. Ошибка получения текущего банковского пользователя"(){
        given:
            def auth = Stub(Authentication)
        when:
            syndicateController.joinTo(Stub(SyndicateJoinRequest), auth)
        then:
            1 * userService.getCurrentUser(auth) >> {throw new UserUnauthorizedException()}
            thrown(UserUnauthorizedException)
    }

    def "Тест. Ошибка получения банка у текущего банковского пользователя"(){
        given:
            def auth = Stub(Authentication)
            def user = new User()
            user.company = new Company()

        when:
            syndicateController.joinTo(Stub(SyndicateJoinRequest), auth)
        then:
            1 * userService.getCurrentUser(auth) >> user
            1 * bankService.getByCompany(user.company) >> null
            thrown(BankNotFoundException)
    }


    def "Тест. Ошибка создания участника синдиката"(){
        given:
            def username = "dross"
            def auth = Stub(Authentication)
            auth.getName() >> username
            def user = new User()
            user.company = new Company()
            def joinRq = new SyndicateJoinRequest()
            def bank = new Bank()
        when:
            syndicateController.joinTo(joinRq, auth)
        then:
            1 * userService.getCurrentUser(auth) >> user
            1 * bankService.getByCompany(user.company) >> bank
            1 * syndicateService.joinBankToSyndicate(joinRq, bank) >> Optional.empty()
            thrown(SyndicateJoinException)
    }

    def "Тест. Присоединение к синдикату"(){
        given:
            def auth = Stub(Authentication)
            def user = new User()
            user.company = new Company()
            def joinRq = new SyndicateJoinRequest()
            def bank = new Bank()
        when:
            syndicateController.joinTo(joinRq, auth)
        then:
            1 * userService.getCurrentUser(auth) >> user
            1 * bankService.getByCompany(user.company) >> bank
            1 * syndicateService.joinBankToSyndicate(joinRq, bank) >> Optional.of(Stub(SyndicateParticipant))
            noExceptionThrown()
    }

    def "Тест. Выход из синдиката"(){
        given:
            def loanRqId = 11
            def auth = Stub(Authentication)
            def user = new User()
            user.company = new Company()
            def bank = new Bank()
        when:
            syndicateController.quitFrom(loanRqId, auth)
        then:
            1 * userService.getCurrentUser(auth) >> user
            1 * bankService.getByCompany(user.company) >> bank
            1 * syndicateParticipantService.quitFromSyndicate(loanRqId, bank)
            noExceptionThrown()
    }
}
