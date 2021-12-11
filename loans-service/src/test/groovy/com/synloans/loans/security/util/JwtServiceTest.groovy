package com.synloans.loans.security.util

import com.synloans.loans.model.entity.User
import com.synloans.loans.security.config.JwtConfig
import spock.lang.Specification

class JwtServiceTest extends Specification{
    private JwtService jwtService
    private JwtConfig jwtConfig

    def setup(){
        jwtConfig = new JwtConfig("secret", 2000)
        jwtService = new JwtService(jwtConfig)
    }

    def "Тест. Генерация токена"(){
        given:
            def user = Stub(User){
                username >> "dross"
            }

        when:
            def dateBeforeExp = new Date(System.currentTimeMillis() + 1000 * jwtConfig.expirationTime - 1000)
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
            def valid = jwtService.validateToken(token, userToCheck)
        then:
            valid == isValid
        where:
            username   || isValid
            "dross"    || true
            "kkfe"     || false
    }
}
