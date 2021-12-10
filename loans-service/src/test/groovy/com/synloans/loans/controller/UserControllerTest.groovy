package com.synloans.loans.controller

import com.synloans.loans.model.authentication.AuthenticationRequest
import com.synloans.loans.model.authentication.RegistrationRequest
import com.synloans.loans.model.entity.Company
import com.synloans.loans.model.entity.User
import com.synloans.loans.security.util.JwtService
import com.synloans.loans.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

class UserControllerTest extends Specification {
    private UserController userController
    private UserService userService
    private AuthenticationManager authenticationManager
    private JwtService jwtService

    def setup(){
        userService = Mock(UserService)
        authenticationManager = Mock(AuthenticationManager)
        jwtService = Mock(JwtService)
        userController = new UserController(userService, authenticationManager, jwtService)
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
            1 * authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password)) >> {throw new BadCredentialsException("")}
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
            def user = Stub(User)
            def generatedToken = "tokenValue"
        when:
            def authResponse = userController.login(authReq)
        then:
            1 * authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password))
            1 * userService.loadUserByUsername(email) >> user
            1 * jwtService.generateToken(user) >> generatedToken
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
                creditOrganisation >> isCreditOrg
            }
            def user = Stub(User)

        when:
            userController.registration(regReq)
        then:
            if (isCreditOrg){
                1 * userService.createBankUser(email, password, _ as Company) >> user
            } else {
                1 * userService.createCorpUser(email, password, _ as Company) >> user
            }
        where:
            isCreditOrg << [false, true]
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
                creditOrganisation >> isCreditOrg
            }

        when:
            userController.registration(regReq)
        then:
            if (isCreditOrg){
                1 * userService.createBankUser(email, password, _ as Company) >> null
            } else {
                1 * userService.createCorpUser(email, password, _ as Company) >> null
            }
            def e = thrown(ResponseStatusException)
            e.status == HttpStatus.UNAUTHORIZED
        where:
            isCreditOrg << [false, true]
    }
}
