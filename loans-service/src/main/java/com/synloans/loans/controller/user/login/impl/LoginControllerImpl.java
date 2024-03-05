package com.synloans.loans.controller.user.login.impl;

import com.synloans.loans.controller.user.login.LoginController;
import com.synloans.loans.model.authentication.AuthenticationRequest;
import com.synloans.loans.model.authentication.AuthenticationResponse;
import com.synloans.loans.model.authentication.token.Token;
import com.synloans.loans.service.exception.UserUnauthorizedException;
import com.synloans.loans.service.exception.advice.response.ErrorResponse;
import com.synloans.loans.service.user.authentication.login.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "Контроллер аутентификации", description = "Обеспечение регистрации и входа пользователей")
@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class LoginControllerImpl implements LoginController {
    
    private final LoginService loginService;

    @Operation(summary = "Авторизация пользователя")
    @ApiResponse(
            responseCode = "200",
            description = "Успешный вход пользователя",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AuthenticationResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Неверный логин или пароль",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Ошибка при валидации тела запроса",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public AuthenticationResponse login(
            @Parameter(required = true, name = "Логин и пароль пользователя")
            @RequestBody @Valid AuthenticationRequest authenticationRequest
    ){
        try{
            Token token = loginService.login(authenticationRequest.getEmail(), authenticationRequest.getPassword());
            return new AuthenticationResponse(token.getValue());
        } catch (BadCredentialsException e){
            throw new UserUnauthorizedException("Неверный логин или пароль");
        }
    }
}
