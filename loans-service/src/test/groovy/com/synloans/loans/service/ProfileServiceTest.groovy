package com.synloans.loans.service

import com.synloans.loans.model.dto.Profile
import com.synloans.loans.model.entity.Bank
import com.synloans.loans.model.entity.Company
import com.synloans.loans.model.entity.User
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
        profileService = new ProfileService(userService, bankService, companyService)
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
            def username = "dross"
            def company = Mock(Company){
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
            def newProfile = Stub(Profile){
                fullName >> "VTB Bank"
                shortName >> "VTB"
                inn >> "123"
                legalAddress >> "New"
                email >> null
            }
        when:
            profileService.editProfile(username, newProfile)
        then:
            1 * userService.getUserByUsername(username) >> user
            1 * company.setFullName(newProfile.fullName)
            1 * company.setShortName(newProfile.shortName)
            1 * company.setInn(newProfile.inn)
            1 * company.setLegalAddress(newProfile.legalAddress)
            0 * userService.saveUser(_)
            1 * companyService.save(company)
    }

    def "Тест. Редактирование информации о компании и email в профиле"(){
        given:
            def username = "dross"
            def company = Mock(Company){
                it.id >> 10
                it.inn >> "123"
                it.kpp >> "345"
                it.fullName >> "SberBank"
                it.shortName >> "Sber"
                it.actualAddress >> "Act Address"
                it.legalAddress >> "Leg Address"
            }
            def user = Mock(User){
                it.username >> username
                it.company >> company
            }
            def newProfile = Stub(Profile){
                fullName >> "VTB Bank"
                shortName >> "VTB"
                inn >> "123"
                legalAddress >> "New"
                email >> "newEmail"
            }
        when:
            profileService.editProfile(username, newProfile)
        then:
            1 * userService.getUserByUsername(username) >> user
            1 * company.setFullName(newProfile.fullName)
            1 * company.setShortName(newProfile.shortName)
            1 * company.setInn(newProfile.inn)
            1 * company.setLegalAddress(newProfile.legalAddress)
            1 * user.setUsername(newProfile.email)
            1 * userService.saveUser(user)
            1 * companyService.save(company)
    }
}
