package com.synloans.loans.controller.user;

import com.synloans.loans.controller.BaseControllerTest;
import com.synloans.loans.model.dto.profile.Profile;
import com.synloans.loans.model.dto.profile.ProfileUpdateRequest;
import com.synloans.loans.service.user.ProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import utils.NoConvertersFilter;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.JsonHelper.asJsonString;


@WebMvcTest(
        value = UserProfileController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = NoConvertersFilter.class)
)
class UserProfileControllerTest extends BaseControllerTest {

    private static final String BASE_PATH = "/profile";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ProfileService profileService;

    @Test
    @DisplayName("Получение профиля пользователя")
    @WithMockUser
    void getProfileTest() throws Exception {
        Profile profile = Profile.builder()
                .fullName("comFull")
                .shortName("comShort")
                .actualAddress("comAct")
                .legalAddress("comLeg")
                .inn("1234567890")
                .kpp("111111111")
                .creditOrganisation(true)
                .email("email@gmail.com")
                .build();


        String username = "user";
        when(profileService.getProfile(username)).thenReturn(profile);

        mockMvc.perform(get(BASE_PATH))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.fullName").value(profile.getFullName()),
                        jsonPath("$.shortName").value(profile.getShortName()),
                        jsonPath("$.actualAddress").value(profile.getActualAddress()),
                        jsonPath("$.legalAddress").value(profile.getLegalAddress()),
                        jsonPath("$.inn").value(profile.getInn()),
                        jsonPath("$.kpp").value(profile.getKpp()),
                        jsonPath("$.creditOrganisation").value(profile.isCreditOrganisation()),
                        jsonPath("$.email").value(profile.getEmail())
                );

        verify(profileService, times(1)).getProfile(username);
    }

    @Test
    @DisplayName("Получение профиля пользователя без авторизации")
    void getProfileAccessDeniedTest() throws Exception {

        mockMvc.perform(get(BASE_PATH))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(blankOrNullString()));

        verify(profileService, never()).getProfile(any());
    }

    @Test
    @DisplayName("Редактирование профиля пользователя без авторизации")
    void editProfileAccessDeniedTest() throws Exception {
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest(
                "newShortName",
                "newLegalAddress",
                "newActualAddress",
                "email@gmail.com"
        );
        mockMvc.perform(
                put(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, updateRequest))
        )
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(blankOrNullString()));

        verify(profileService, never()).editProfile(any(), any());
    }

    @ParameterizedTest
    @MethodSource("validProfileUpdates")
    @DisplayName("Редактирование профиля пользователя")
    @WithMockUser
    void editProfileTest(ProfileUpdateRequest updateRequest) throws Exception {
        String username = "user";

        mockMvc.perform(
                put(BASE_PATH + "/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, updateRequest))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(blankOrNullString()));

        verify(profileService, times(1)).editProfile(username, updateRequest);
    }

    @ParameterizedTest
    @MethodSource("notValidProfileUpdates")
    @DisplayName("Редактирование профиля пользователя с навалидными данными")
    @WithMockUser
    void editProfileWithInvalidDataTest(ProfileUpdateRequest updateRequest) throws Exception {
        String username = "user";

        mockMvc.perform(
                put(BASE_PATH + "/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, updateRequest))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.code").value(400),
                        jsonPath("$.status").value("BAD_REQUEST"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").exists(),
                        jsonPath("$.timestamp").exists()
                );

        verify(profileService, never()).editProfile(username, updateRequest);
    }

    private static Stream<Arguments> notValidProfileUpdates() {
        return Stream.of(
                Arguments.of(new ProfileUpdateRequest(
                        "",
                        "l",
                        "a",
                        "email@gmail.com"
                )),
                Arguments.of(new ProfileUpdateRequest(
                        "s",
                        "",
                        "a",
                        "email@gmail.com"
                )),
                Arguments.of(new ProfileUpdateRequest(
                        "s",
                        "l",
                        "",
                        "email@gmail.com"
                )),
                Arguments.of(new ProfileUpdateRequest(
                        "s",
                        "l",
                        "a",
                        "email"
                )),
                Arguments.of(new ProfileUpdateRequest(
                        "s",
                        "l",
                        "a",
                        ""
                ))
        );
    }

    private static Stream<Arguments> validProfileUpdates() {
        return Stream.of(
                Arguments.of(new ProfileUpdateRequest(
                        null,
                        "l",
                        "a",
                        "email@gmail.com"
                )),
                Arguments.of(new ProfileUpdateRequest(
                        "s",
                        null,
                        "a",
                        "email@gmail.com"
                )),
                Arguments.of(new ProfileUpdateRequest(
                        "s",
                        "l",
                        null,
                        "email@gmail.com"
                )),
                Arguments.of(new ProfileUpdateRequest(
                        "s",
                        "l",
                        "a",
                        null
                )),
                Arguments.of(new ProfileUpdateRequest(
                        "newShortName",
                        "newLegalAddress",
                        "newActualAddress",
                        "email@gmail.com"
                ))
        );
    }



}