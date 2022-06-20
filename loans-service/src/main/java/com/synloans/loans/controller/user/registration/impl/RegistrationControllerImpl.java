package com.synloans.loans.controller.user.registration.impl;

import com.synloans.loans.controller.user.registration.RegistrationController;
import com.synloans.loans.model.authentication.RegistrationRequest;
import com.synloans.loans.service.exception.advice.response.ErrorResponse;
import com.synloans.loans.service.user.authentication.registration.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class RegistrationControllerImpl implements RegistrationController {

    private final RegistrationService registrationService;

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
    @Override
    public void registration(
            @Parameter(required = true, name = "Данные о новом пользователе")
            @RequestBody @Valid RegistrationRequest registrationRequest
    ) {
        registrationService.register(registrationRequest);
    }
}
