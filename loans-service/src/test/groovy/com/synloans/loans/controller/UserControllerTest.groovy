package com.synloans.loans.controller

import com.synloans.loans.model.authentication.AuthenticationRequest
import com.synloans.loans.model.authentication.RegistrationRequest
import com.synloans.loans.model.entity.Company
import com.synloans.loans.service.UserService
import com.synloans.loans.service.exception.CreateUserException
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

class UserControllerTest extends Specification {
    private UserController userController
    private UserService userService

    def setup(){
        userService = Mock(UserService)
        userController = new UserController(userService)
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
            def authResponse = userController.login(authReq)
        then:
            1 * userService.login(email, password) >> {throw new BadCredentialsException("")}
            def e = thrown(ResponseStatusException)
            e.status == HttpStatus.UNAUTHORIZED
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
            def authResponse = userController.login(authReq)
        then:
            1 * userService.login(email, password) >> generatedToken
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
            userController.registration(regReq)
        then:
            1 * userService.createUser(email, password, _ as Company, regReq.creditOrganisation)
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
            userController.registration(regReq)
        then:
            1 * userService.createUser(email, password, _ as Company, regReq.creditOrganisation) >> {throw new CreateUserException()}
            thrown(CreateUserException)
    }
}
