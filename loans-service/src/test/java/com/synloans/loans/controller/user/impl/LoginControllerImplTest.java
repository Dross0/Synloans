package com.synloans.loans.controller.user.impl;

import com.synloans.loans.controller.BaseControllerTest;
import com.synloans.loans.controller.user.login.impl.LoginControllerImpl;
import com.synloans.loans.model.authentication.AuthenticationRequest;
import com.synloans.loans.model.authentication.token.Token;
import com.synloans.loans.model.authentication.token.impl.JwtToken;
import com.synloans.loans.service.user.authentication.login.LoginService;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import utils.NoConvertersFilter;

import java.time.Instant;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
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
        value = LoginControllerImpl.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = NoConvertersFilter.class)
)
class LoginControllerImplTest extends BaseControllerTest {

    private static final String BASE_PATH = "";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    LoginService loginService;

    @Test
    @DisplayName("Неверный логин или пароль при входе")
    void loginWithIncorrectLoginOrPasswordTest() throws Exception {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                "email@gmail.com",
                "qwerty123"
        );

        when(loginService.login(authenticationRequest.getEmail(), authenticationRequest.getPassword()))
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

        verify(loginService, times(1))
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

        when(loginService.login(authenticationRequest.getEmail(), authenticationRequest.getPassword()))
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

        verify(loginService, times(1))
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

        verify(loginService, never()).login(anyString(), anyString());
    }

    @Test
    @DisplayName("Успешный вход")
    void loginTest() throws Exception {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                "email@gmail.com",
                "qwerty123"
        );

        Token token = new JwtToken(
                "jwt-toke2323231443",
                "usrw",
                Instant.now(),
                Instant.now()
        );

        when(loginService.login(authenticationRequest.getEmail(), authenticationRequest.getPassword()))
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
                        jsonPath("$.token").value(token.getValue())
                );

        verify(loginService, times(1))
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

        Token token = new JwtToken(
                "jwt-toke2323231443",
                "usrw",
                Instant.now(),
                Instant.now()
        );

        when(loginService.login(authenticationRequest.getEmail(), authenticationRequest.getPassword()))
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
                        jsonPath("$.token").value(token.getValue())
                );

        verify(loginService, times(1))
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
}