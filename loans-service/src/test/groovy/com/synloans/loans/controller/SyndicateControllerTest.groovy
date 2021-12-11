package com.synloans.loans.controller

import com.synloans.loans.model.dto.LoanSum
import com.synloans.loans.model.dto.SyndicateJoinRequest
import com.synloans.loans.model.entity.Bank
import com.synloans.loans.model.entity.Company
import com.synloans.loans.model.entity.SyndicateParticipant
import com.synloans.loans.model.entity.User
import com.synloans.loans.service.BankService
import com.synloans.loans.service.SyndicateParticipantService
import com.synloans.loans.service.SyndicateService
import com.synloans.loans.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.server.ResponseStatusException
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
            def username = "dross"
            def auth = Stub(Authentication)
            auth.getName() >> username
        when:
            syndicateController.joinTo(Stub(SyndicateJoinRequest), auth)
        then:
            1 * userService.getUserByUsername(username) >> null
            def e = thrown(ResponseStatusException)
            e.status == HttpStatus.UNAUTHORIZED
    }

    def "Тест. Ошибка получения банка у текущего банковского пользователя"(){
        given:
            def username = "dross"
            def auth = Stub(Authentication)
            auth.getName() >> username
            def user = Stub(User){
                company >> Stub(Company)
            }
        when:
            syndicateController.joinTo(Stub(SyndicateJoinRequest), auth)
        then:
            1 * userService.getUserByUsername(username) >> user
            1 * bankService.getByCompany(user.company) >> null
            def e = thrown(ResponseStatusException)
            e.status == HttpStatus.NOT_FOUND
    }


    def "Тест. Ошибка создания участника синдиката"(){
        given:
            def username = "dross"
            def auth = Stub(Authentication)
            auth.getName() >> username
            def user = Stub(User){
                company >> Stub(Company)
            }
            def joinRq = Stub(SyndicateJoinRequest)
            def bank = Stub(Bank)
        when:
            syndicateController.joinTo(joinRq, auth)
        then:
            1 * userService.getUserByUsername(username) >> user
            1 * bankService.getByCompany(user.company) >> bank
            1 * syndicateService.joinBankToSyndicate(joinRq, bank) >> Optional.empty()
            def e = thrown(ResponseStatusException)
            e.status == HttpStatus.INTERNAL_SERVER_ERROR
    }

    def "Тест. Присоединение к синдикату"(){
        given:
            def username = "dross"
            def auth = Stub(Authentication)
            auth.getName() >> username
            def user = Stub(User){
                company >> Stub(Company)
            }
            def joinRq = Stub(SyndicateJoinRequest){
                requestId >> 20
                sum >> LoanSum.valueOf(100_000)
                approveBankAgent >> true
            }
            def bank = Stub(Bank)
        when:
            syndicateController.joinTo(joinRq, auth)
        then:
            1 * userService.getUserByUsername(username) >> user
            1 * bankService.getByCompany(user.company) >> bank
            1 * syndicateService.joinBankToSyndicate(joinRq, bank) >> Optional.of(Stub(SyndicateParticipant))
            noExceptionThrown()
    }

    def "Тест. Выход из синдиката"(){
        given:
            def loanRqId = 11
            def username = "dross"
            def auth = Stub(Authentication)
            auth.getName() >> username
            def user = Stub(User){
                company >> Stub(Company)
            }
            def bank = Stub(Bank)
        when:
            syndicateController.quitFrom(loanRqId, auth)
        then:
            1 * userService.getUserByUsername(username) >> user
            1 * bankService.getByCompany(user.company) >> bank
            1 * syndicateParticipantService.quitFromSyndicate(loanRqId, bank)
            noExceptionThrown()
    }
}
