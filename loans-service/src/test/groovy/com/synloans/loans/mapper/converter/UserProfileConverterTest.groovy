package com.synloans.loans.mapper.converter


import com.synloans.loans.model.dto.profile.Profile
import com.synloans.loans.model.entity.company.Company
import com.synloans.loans.model.entity.user.User
import spock.lang.Specification

class UserProfileConverterTest extends Specification{

    private UserProfileConverter userProfileConverter

    def setup(){
        userProfileConverter = new UserProfileConverter()
    }

    def "Тест. Пользователь в профиль"(){
        given:
            Company company = new Company()
            company.id = 1
            company.fullName = "fullName"
            company.shortName = "shortName"
            company.inn = "123"
            company.kpp = "234"
            company.actualAddress = "actualAddress"
            company.legalAddress = "legalAddress"

            User user = new User()
            user.id = 1
            user.company = company
            user.password = "password"
            user.username = "ecf.ru"

        when:
            Profile profile = userProfileConverter.convert(user)
        then:
            verifyAll(profile){
                email == user.username
                fullName == company.fullName
                shortName == company.shortName
                inn == company.inn
                kpp == company.kpp
                actualAddress == company.actualAddress
                legalAddress == company.legalAddress
            }
    }
}
