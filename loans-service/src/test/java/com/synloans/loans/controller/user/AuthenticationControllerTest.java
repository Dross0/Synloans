package com.synloans.loans.controller.user;

import com.synloans.loans.controller.BaseControllerTest;
import com.synloans.loans.model.authentication.AuthenticationRequest;
import com.synloans.loans.model.authentication.RegistrationRequest;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.service.exception.CreateUserException;
import com.synloans.loans.service.exception.notfound.CompanyNotFoundException;
import com.synloans.loans.service.user.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import utils.NoConvertersFilter;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.JsonHelper.asJsonString;


@WebMvcTest(
        value = AuthenticationController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = NoConvertersFilter.class)
)
class AuthenticationControllerTest extends BaseControllerTest {

    private static final String BASE_PATH = "";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AuthenticationService authenticationService;

    @Captor
    ArgumentCaptor<Company> companyArgumentCaptor;

    @Test
    @DisplayName("Неверный логин или пароль при входе")
    void loginWithIncorrectLoginOrPasswordTest() throws Exception {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                "email@gmail.com",
                "qwerty123"
        );

        when(authenticationService.login(authenticationRequest.getEmail(), authenticationRequest.getPassword()))
                .thenThrow(BadCredentialsException.class);

        mockMvc.perform(
                post(BASE_PATH + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(objectMapper, authenticationRequest))
        )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.code").value(401),
                        jsonPath("$.status").value("UNAUTHORIZED"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").exists(),
                        jsonPath("$.timestamp").exists()
                );

        verify(authenticationService, times(1))
                .login(authenticationRequest.getEmail(), authenticationRequest.getPassword());
    }

    @Test
    @DisplayName("Неверный логин или пароль при повторном входе")
    @WithMockUser
    void loginWithIncorrectLoginOrPasswordWhenUserAuthenticatedTest() throws Exception {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                "email@gmail.com",
                "qwerty123"
        );

        when(authenticationService.login(authenticationRequest.getEmail(), authenticationRequest.getPassword()))
                .thenThrow(BadCredentialsException.class);

        mockMvc.perform(
                post(BASE_PATH + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(objectMapper, authenticationRequest))
        )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.code").value(401),
                        jsonPath("$.status").value("UNAUTHORIZED"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").exists(),
                        jsonPath("$.timestamp").exists()
                );

        verify(authenticationService, times(1))
                .login(authenticationRequest.getEmail(), authenticationRequest.getPassword());
    }

    @ParameterizedTest
    @MethodSource("notValidLoginCreds")
    @DisplayName("Невалидные данные для входа")
    void loginWithInvalidDataTest(AuthenticationRequest authenticationRequest) throws Exception {

        mockMvc.perform(
                post(BASE_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, authenticationRequest))
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

        verify(authenticationService, never()).login(anyString(), anyString());
    }

    @Test
    @DisplayName("Успешный вход")
    void loginTest() throws Exception {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                "email@gmail.com",
                "qwerty123"
        );

        String token = "jwt-toke2323231443";

        when(authenticationService.login(authenticationRequest.getEmail(), authenticationRequest.getPassword()))
                .thenReturn(token);

        mockMvc.perform(
                post(BASE_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, authenticationRequest))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.token").value(token)
                );

        verify(authenticationService, times(1))
                .login(authenticationRequest.getEmail(), authenticationRequest.getPassword());
    }

    @Test
    @DisplayName("Успешный вход, когда пользователь уже зайден")
    @WithMockUser
    void loginWhenUserAuthenticatedTest() throws Exception {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                "email@gmail.com",
                "qwerty123"
        );

        String token = "jwt-toke2323231443";

        when(authenticationService.login(authenticationRequest.getEmail(), authenticationRequest.getPassword()))
                .thenReturn(token);

        mockMvc.perform(
                post(BASE_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, authenticationRequest))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.token").value(token)
                );

        verify(authenticationService, times(1))
                .login(authenticationRequest.getEmail(), authenticationRequest.getPassword());
    }

    private static Stream<Arguments> notValidLoginCreds() {
        return Stream.of(
                Arguments.of(
                        new AuthenticationRequest(
                                "rfefe",
                                "qwerty12345"
                        )
                ),
                Arguments.of(
                        new AuthenticationRequest(
                                "",
                                "qwerty12345"
                        )
                ),
                Arguments.of(
                        new AuthenticationRequest(
                                "rfefe@mail.ru",
                                "1234567"
                        )
                ),
                Arguments.of(
                        new AuthenticationRequest(
                                "rfefe@mail.ru",
                                ""
                        )
                ),
                Arguments.of(
                        new AuthenticationRequest(
                                null,
                                "qwerty12345"
                        )
                ),
                Arguments.of(
                        new AuthenticationRequest(
                                "rfefe@gmail.com",
                                null
                        )
                ),
                Arguments.of(
                        new AuthenticationRequest(
                                null,
                                null
                        )
                )
        );
    }

    @Test
    @DisplayName("Регистрации, когда email уже существует")
    void registrationWithAlreadyRegisteredEmailTest() throws Exception {
        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .email("a@gmail.com")
                .actualAddress("comAA")
                .legalAddress("comLA")
                .fullName("comFN")
                .shortName("comSN")
                .creditOrganisation(true)
                .inn("1234567890")
                .kpp("123456789")
                .password("qwerty1234")
                .build();

        when(authenticationService.register(
                eq(registrationRequest.getEmail()),
                eq(registrationRequest.getPassword()),
                any(Company.class),
                eq(registrationRequest.isCreditOrganisation())
        )).thenThrow(CreateUserException.class);

        mockMvc.perform(
                post(BASE_PATH + "/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, registrationRequest))
        )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.code").value(401),
                        jsonPath("$.status").value("UNAUTHORIZED"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(authenticationService, times(1))
                .register(
                        eq(registrationRequest.getEmail()),
                        eq(registrationRequest.getPassword()),
                        any(Company.class),
                        eq(registrationRequest.isCreditOrganisation())
                );
    }

    @Test
    @DisplayName("Ошибка поиска/создания компании при регистрации")
    void registrationNotFoundCompanyTest() throws Exception {
        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .email("a@gmail.com")
                .actualAddress("comAA")
                .legalAddress("comLA")
                .fullName("comFN")
                .shortName("comSN")
                .creditOrganisation(true)
                .inn("1234567890")
                .kpp("123456789")
                .password("qwerty1234")
                .build();

        when(authenticationService.register(
                eq(registrationRequest.getEmail()),
                eq(registrationRequest.getPassword()),
                any(Company.class),
                eq(registrationRequest.isCreditOrganisation())
        )).thenThrow(CompanyNotFoundException.class);

        mockMvc.perform(
                post(BASE_PATH + "/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, registrationRequest))
        )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.code").value(404),
                        jsonPath("$.status").value("NOT_FOUND"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(authenticationService, times(1))
                .register(
                        eq(registrationRequest.getEmail()),
                        eq(registrationRequest.getPassword()),
                        any(Company.class),
                        eq(registrationRequest.isCreditOrganisation())
                );
    }

    @Test
    @DisplayName("Успешная регистрация")
    void registrationTest() throws Exception {
        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .email("dww@gmail.com")
                .actualAddress("comAA")
                .legalAddress("comLA")
                .fullName("comFN")
                .shortName("comSN")
                .creditOrganisation(true)
                .inn("1234567890")
                .kpp("123456789")
                .password("qwerty1234")
                .build();

        mockMvc.perform(
                post(BASE_PATH + "/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, registrationRequest))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(blankOrNullString()));

        verify(authenticationService, times(1))
                .register(
                        eq(registrationRequest.getEmail()),
                        eq(registrationRequest.getPassword()),
                        companyArgumentCaptor.capture(),
                        eq(registrationRequest.isCreditOrganisation())
                );

        Company company = companyArgumentCaptor.getValue();

        assertThat(company.getFullName()).isEqualTo(registrationRequest.getFullName());
        assertThat(company.getShortName()).isEqualTo(registrationRequest.getShortName());
        assertThat(company.getInn()).isEqualTo(registrationRequest.getInn());
        assertThat(company.getKpp()).isEqualTo(registrationRequest.getKpp());
        assertThat(company.getActualAddress()).isEqualTo(registrationRequest.getActualAddress());
        assertThat(company.getLegalAddress()).isEqualTo(registrationRequest.getLegalAddress());
    }

    @Test
    @DisplayName("Успешная регистрация, когда пользователь авторизован")
    @WithMockUser
    void registrationWhenUserAuthenticatedTest() throws Exception {
        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .email("dww@gmail.com")
                .actualAddress("comAA")
                .legalAddress("comLA")
                .fullName("comFN")
                .shortName("comSN")
                .creditOrganisation(true)
                .inn("1234567890")
                .kpp("123456789")
                .password("qwerty1234")
                .build();

        mockMvc.perform(
                post(BASE_PATH + "/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, registrationRequest))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(blankOrNullString()));

        verify(authenticationService, times(1))
                .register(
                        eq(registrationRequest.getEmail()),
                        eq(registrationRequest.getPassword()),
                        companyArgumentCaptor.capture(),
                        eq(registrationRequest.isCreditOrganisation())
                );

        Company company = companyArgumentCaptor.getValue();

        assertThat(company.getFullName()).isEqualTo(registrationRequest.getFullName());
        assertThat(company.getShortName()).isEqualTo(registrationRequest.getShortName());
        assertThat(company.getInn()).isEqualTo(registrationRequest.getInn());
        assertThat(company.getKpp()).isEqualTo(registrationRequest.getKpp());
        assertThat(company.getActualAddress()).isEqualTo(registrationRequest.getActualAddress());
        assertThat(company.getLegalAddress()).isEqualTo(registrationRequest.getLegalAddress());
    }

    @ParameterizedTest
    @MethodSource("notValidRegistrationRequest")
    @DisplayName("Ошибка поиска/создания компании при регистрации")
    void registrationWithInvalidRequestTest(RegistrationRequest registrationRequest) throws Exception {

        mockMvc.perform(
                post(BASE_PATH + "/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, registrationRequest))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.code").value(400),
                        jsonPath("$.status").value("BAD_REQUEST"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(authenticationService, never())
                .register(anyString(), anyString(), any(Company.class), anyBoolean());
    }

    private static RegistrationRequest brokenRequest(Consumer<RegistrationRequest> breakAction){
        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .email("dww@gmail.com")
                .actualAddress("comAA")
                .legalAddress("comLA")
                .fullName("comFN")
                .shortName("comSN")
                .creditOrganisation(true)
                .inn("1234567890")
                .kpp("123456789")
                .password("qwerty1234")
                .build();
        breakAction.accept(registrationRequest);
        return registrationRequest;
    }

    private static Stream<Arguments> notValidRegistrationRequest() {
        return Stream.of(
                Arguments.of(
                        brokenRequest(r -> r.setEmail("23131d"))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setEmail(null))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setEmail(""))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setActualAddress(null))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setActualAddress(""))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setLegalAddress(null))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setLegalAddress(""))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setFullName(null))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setFullName(""))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setShortName(null))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setShortName(""))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setInn("123456789"))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setInn("12345678901"))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setInn(null))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setInn(""))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setKpp("12345678"))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setKpp("1234567890"))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setKpp(null))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setKpp(""))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setPassword(null))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setPassword(""))
                ),
                Arguments.of(
                        brokenRequest(r -> r.setPassword("qwer123"))
                )
        );
    }
}