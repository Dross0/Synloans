package com.synloans.loans.controller.user

import com.synloans.loans.model.authentication.AuthenticationRequest
import com.synloans.loans.model.authentication.RegistrationRequest
import com.synloans.loans.model.entity.company.Company
import com.synloans.loans.service.exception.CreateUserException
import com.synloans.loans.service.exception.UserUnauthorizedException
import com.synloans.loans.service.user.AuthenticationService
import org.springframework.security.authentication.BadCredentialsException
import spock.lang.Specification

class AuthenticationControllerTest extends Specification {
    private AuthenticationController authenticationController
    private AuthenticationService authenticationService

    def setup(){
        authenticationService = Mock(AuthenticationService)
        authenticationController = new AuthenticationController(authenticationService)
    }

    def "Тест. Неуспешный логин пользователя"(){
        given:
            def email = "email@abc.ru"
            def password = "qwerty"
            def authReq = Stub(AuthenticationRequest){
                it.email >> email
                it.password >> password
            }
        when:
            authenticationController.login(authReq)
        then:
            1 * authenticationService.login(email, password) >> {throw new BadCredentialsException("")}
            thrown(UserUnauthorizedException)
    }

    def "Тест. Успешный логин пользователя"(){
        given:
            def email = "email@abc.ru"
            def password = "qwerty"
            def authReq = Stub(AuthenticationRequest){
                it.email >> email
                it.password >> password
            }
            def generatedToken = "tokenValue"
        when:
            def authResponse = authenticationController.login(authReq)
        then:
            1 * authenticationService.login(email, password) >> generatedToken
            authResponse.token == generatedToken
    }

    def "Тест. Успешная регистрация пользователя"(){
        given:
            def email = "email@abc.ru"
            def password = "qwerty"
            def regReq = Stub(RegistrationRequest){
                it.email >> email
                it.password >> password
                it.fullName >> "SberBank"
                it.shortName >> "Sber"
                it.inn >> "123"
                it.kpp >> "664"
                it.actualAddress >> "Act Addr"
                it.legalAddress >> "Leg Addr"
                it.creditOrganisation >> true
            }
        when:
            authenticationController.registration(regReq)
        then:
            1 * authenticationService.register(email, password, _ as Company, regReq.creditOrganisation)
            noExceptionThrown()
    }

    def "Тест. Неуспешная регистрация пользователя"(){
        given:
            def email = "email@abc.ru"
            def password = "qwerty"
            def regReq = Stub(RegistrationRequest){
                it.email >> email
                it.password >> password
                it.fullName >> "SberBank"
                it.shortName >> "Sber"
                it.inn >> "123"
                it.kpp >> "664"
                it.actualAddress >> "Act Addr"
                it.legalAddress >> "Leg Addr"
                it.creditOrganisation >> true
            }
        when:
            authenticationController.registration(regReq)
        then:
            1 * authenticationService.register(email, password, _ as Company, regReq.creditOrganisation) >> {throw new CreateUserException()}
            thrown(CreateUserException)
    }
}
