package com.synloans.loans.service.user.authentication.login.impl;

import com.synloans.loans.model.authentication.token.Token;
import com.synloans.loans.service.token.generator.TokenGenerator;
import com.synloans.loans.service.user.authentication.login.LoginService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LoginServiceImpl.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoginServiceImplTest {

    @Autowired
    LoginService loginService;

    @MockBean
    TokenGenerator tokenGenerator;

    @MockBean
    UserDetailsService userDetailsService;

    @MockBean
    AuthenticationManager authenticationManager;

    @Captor
    ArgumentCaptor<Authentication> authenticationArgumentCaptor;

    @Test
    @DisplayName("Успешный вход")
    void loginTest(){
        String username = "user1";
        String password = "qwerty";

        Token expectedToken = Mockito.mock(Token.class);

        UserDetails userDetails = Mockito.mock(UserDetails.class);

        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(tokenGenerator.generateToken(userDetails)).thenReturn(expectedToken);

        Token token = loginService.login(username, password);

        verify(authenticationManager, times(1))
                .authenticate(authenticationArgumentCaptor.capture());
        verify(tokenGenerator, times(1)).generateToken(userDetails);
        verify(userDetailsService, times(1)).loadUserByUsername(username);

        Authentication authentication = authenticationArgumentCaptor.getValue();
        assertThat(authentication.getName()).isEqualTo(username);
        assertThat(authentication.getCredentials()).isEqualTo(password);
        assertThat(token).isEqualTo(expectedToken);
    }

    @Test
    @DisplayName("Вход по неверным учетным данным")
    void loginWithIncorrectCredsTest(){
        String username = "user1";
        String password = "qwerty";

        when(authenticationManager.authenticate(any())).thenThrow(BadCredentialsException.class);

        Throwable throwable = catchThrowable(() -> loginService.login(username, password));
        assertThat(throwable).isInstanceOf(BadCredentialsException.class);

        verify(authenticationManager, times(1))
                .authenticate(authenticationArgumentCaptor.capture());
        verify(tokenGenerator, never()).generateToken(any());
        verify(userDetailsService, never()).loadUserByUsername(any());

        Authentication authentication = authenticationArgumentCaptor.getValue();
        assertThat(authentication.getName()).isEqualTo(username);
        assertThat(authentication.getCredentials()).isEqualTo(password);
    }

    @Test
    @DisplayName("Пользователь не найден")
    void loginUserNotFoundTest(){
        String username = "user1";
        String password = "qwerty";

        when(userDetailsService.loadUserByUsername(username)).thenThrow(UsernameNotFoundException.class);

        Throwable throwable = catchThrowable(() -> loginService.login(username, password));
        assertThat(throwable).isInstanceOf(UsernameNotFoundException.class);

        verify(authenticationManager, times(1))
                .authenticate(authenticationArgumentCaptor.capture());
        verify(tokenGenerator, never()).generateToken(any());
        verify(userDetailsService, times(1)).loadUserByUsername(username);

        Authentication authentication = authenticationArgumentCaptor.getValue();
        assertThat(authentication.getName()).isEqualTo(username);
        assertThat(authentication.getCredentials()).isEqualTo(password);
    }

}