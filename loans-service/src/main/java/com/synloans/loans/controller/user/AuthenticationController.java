package com.synloans.loans.controller.user;

import com.synloans.loans.model.authentication.AuthenticationRequest;
import com.synloans.loans.model.authentication.AuthenticationResponse;
import com.synloans.loans.model.authentication.RegistrationRequest;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.service.exception.UserUnauthorizedException;
import com.synloans.loans.service.exception.advice.response.ErrorResponse;
import com.synloans.loans.service.user.AuthenticationService;
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
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    private final AuthenticationService authenticationService;

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
    public AuthenticationResponse login(
            @Parameter(required = true, name = "Логин и пароль пользователя")
            @RequestBody @Valid AuthenticationRequest authenticationRequest
    ){
        try{
            String jwt = authenticationService.login(authenticationRequest.getEmail(), authenticationRequest.getPassword());
            return new AuthenticationResponse(jwt);
        } catch (BadCredentialsException e){
            throw new UserUnauthorizedException("Неверный логин или пароль");
        }
    }

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Если при регистрации пользователь указывает реквизиты существующей компании, " +
                    "то он регистрируется как пользователь этой компании, иначе компания с данными реквизитами создается"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Пользователь успешно зарегистрирован"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Ошибка поиска или создания компаниии пользователя",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Пользователь с таким email уже зарегистрирован",
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
    @PostMapping(value = "/registration", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void registration(
            @Parameter(required = true, name = "Данные о новом пользователе")
            @RequestBody @Valid RegistrationRequest registrationRequest
    ) {
        Company company = new Company();
        company.setInn(registrationRequest.getInn());
        company.setKpp(registrationRequest.getKpp());
        company.setFullName(registrationRequest.getFullName());
        company.setShortName(registrationRequest.getShortName());
        company.setActualAddress(registrationRequest.getActualAddress());
        company.setLegalAddress(registrationRequest.getLegalAddress());
        authenticationService.register(
                registrationRequest.getEmail(),
                registrationRequest.getPassword(),
                company,
                registrationRequest.isCreditOrganisation()
        );
    }
}
