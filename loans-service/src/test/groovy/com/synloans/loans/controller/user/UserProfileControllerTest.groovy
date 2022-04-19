package com.synloans.loans.controller.user


import com.synloans.loans.model.dto.profile.Profile
import com.synloans.loans.model.dto.profile.ProfileUpdateRequest
import com.synloans.loans.service.user.ProfileService
import org.springframework.security.core.Authentication
import spock.lang.Specification

class UserProfileControllerTest extends Specification{
    private UserProfileController userProfileController
    private ProfileService profileService

    def setup(){
        profileService = Mock(ProfileService)
        userProfileController = new UserProfileController(profileService)
    }

    def "Тест. Получение профиля"(){
        given:
            def username = "dross"
            def auth = Stub(Authentication)
            auth.getName() >> username
            def profile = Stub(Profile)
        when:
            def result = userProfileController.getProfile(auth)
        then:
            1 * profileService.getProfile(username) >> profile
            result == profile
    }

    def "Тест. Редактирование профиля"(){
        given:
            def username = "dross"
            def auth = Stub(Authentication)
            auth.getName() >> username
            def updateRequest = new ProfileUpdateRequest()
        when:
            userProfileController.editProfile(updateRequest, auth)
        then:
            1 * profileService.editProfile(username, updateRequest)
    }

}
