package com.synloans.loans.security.util

import com.synloans.loans.configuration.properties.security.JwtProperties
import com.synloans.loans.model.entity.user.User
import spock.lang.Specification

import java.time.Duration

class JwtServiceTest extends Specification{
    private JwtService jwtService
    private JwtProperties jwtConfig

    def setup(){
        jwtConfig = new JwtProperties("secret", Duration.ofSeconds(2000))
        jwtService = new JwtService(jwtConfig)
    }

    def "Тест. Генерация токена"(){
        given:
            def user = Stub(User){
                username >> "dross"
            }

        when:
            def dateBeforeExp = new Date(System.currentTimeMillis() + jwtConfig.expirationTime.toMillis() - 1000)
            def token = jwtService.generateToken(user)
            def usernameFromToken = jwtService.extractUsername(token)
            def expDate = jwtService.extractExpiration(token)
        then:
            usernameFromToken == user.username
            dateBeforeExp.before(expDate)
            expDate.after(new Date())
    }

    def "Тест. Валидация токена"(){
        given:
            def user = Stub(User){
                username >> "dross"
            }
        when:
            def token = jwtService.generateToken(user)
            def userToCheck = Stub(User){
                it.username >> username
            }
            def valid = jwtService.isValidToken(token, userToCheck)
        then:
            valid == isValid
        where:
            username   || isValid
            "dross"    || true
            "kkfe"     || false
    }
}
