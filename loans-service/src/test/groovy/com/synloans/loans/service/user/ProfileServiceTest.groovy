package com.synloans.loans.service.user

import com.synloans.loans.mapper.converter.UserProfileConverter
import com.synloans.loans.model.dto.profile.ProfileUpdateRequest
import com.synloans.loans.model.entity.company.Bank
import com.synloans.loans.model.entity.company.Company
import com.synloans.loans.model.entity.user.User
import com.synloans.loans.service.company.BankService
import com.synloans.loans.service.company.CompanyService
import spock.lang.Specification

class ProfileServiceTest extends Specification {
    private ProfileService profileService
    private UserService userService
    private BankService bankService
    private CompanyService companyService

    def setup(){
        userService = Mock(UserService)
        bankService = Mock(BankService)
        companyService = Mock(CompanyService)
        profileService = new ProfileService(userService, bankService, companyService, new UserProfileConverter())
    }

    def "Тест. Получение профиля"(){
        given:
            def username = "dross"
            def company = Stub(Company){
                it.id >> 10
                it.inn >> "123"
                it.kpp >> "345"
                it.fullName >> "SberBank"
                it.shortName >> "Sber"
                it.actualAddress >> "Act Address"
                it.legalAddress >> "Leg Address"
            }
            def user = Stub(User){
                it.username >> username
                it.company >> company
            }
        when:
            def profile = profileService.getProfile(username)
        then:
            1 * userService.getUserByUsername(username) >> user
            1 * bankService.getByCompany(user.getCompany()) >> bank
            with(profile){
                email == username
                inn == company.inn
                kpp == company.kpp
                fullName == company.fullName
                shortName == company.shortName
                actualAddress == company.actualAddress
                legalAddress == company.legalAddress
                creditOrganisation == isCredit
            }
        where:
            bank       || isCredit
            null       || false
            Stub(Bank) || true
    }

    def "Тест. Редактирование информации о компании в профиле"(){
        given:
            def inn = "123"
            def kpp = "345"
            def fullName = "PAO Sber"

            def username = "dross"
            Company company = new Company()
            company.id = 10
            company.inn = inn
            company.kpp = kpp
            company.fullName = fullName
            company.shortName = "Sber"
            company.actualAddress = "Act"
            company.legalAddress = "leg"

            def user = new User()
            user.company = company
            user.username = username

            def profileUpdateRequest = new ProfileUpdateRequest()
            profileUpdateRequest.shortName = "New Short"
            profileUpdateRequest.legalAddress = "New Legal address"
            profileUpdateRequest.actualAddress = "New Actual address"
        when:
            profileService.editProfile(username, profileUpdateRequest)
        then:
            1 * userService.getUserByUsername(username) >> user
            0 * userService.saveUser(_)
            1 * companyService.save(company)

            verifyAll(company){
                id == 10
                it.inn == inn
                it.kpp == kpp
                it.fullName == fullName
                shortName == profileUpdateRequest.shortName
                legalAddress == profileUpdateRequest.legalAddress
                actualAddress == profileUpdateRequest.actualAddress
            }
    }

    def "Тест. Редактирование информации о компании и email в профиле"(){
        given:
            def inn = "123"
            def kpp = "345"
            def fullName = "PAO Sber"
            def actualAddress = "act"

            def username = "dross"
            Company company = new Company()
            company.id = 10
            company.inn = inn
            company.kpp = kpp
            company.fullName = fullName
            company.shortName = "Sber"
            company.actualAddress = actualAddress
            company.legalAddress = "leg"

            def user = new User()
            user.company = company
            user.username = username

            def profileUpdateRequest = new ProfileUpdateRequest()
            profileUpdateRequest.shortName = "New Short"
            profileUpdateRequest.legalAddress = "New Legal address"
            profileUpdateRequest.email = "newEmail"

        when:
            profileService.editProfile(username, profileUpdateRequest)
        then:
            1 * userService.getUserByUsername(username) >> user
            1 * userService.saveUser(user)
            1 * companyService.save(company)

            user.username == profileUpdateRequest.email

            verifyAll(company){
                id == 10
                it.inn == inn
                it.kpp == kpp
                it.fullName == fullName
                shortName == profileUpdateRequest.shortName
                legalAddress == profileUpdateRequest.legalAddress
                it.actualAddress == actualAddress
            }

    }
}
