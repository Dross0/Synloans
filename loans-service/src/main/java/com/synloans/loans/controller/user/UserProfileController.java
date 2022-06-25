package com.synloans.loans.controller.user;

import com.synloans.loans.model.dto.profile.Profile;
import com.synloans.loans.model.dto.profile.ProfileUpdateRequest;
import com.synloans.loans.service.exception.advice.response.ErrorResponse;
import com.synloans.loans.service.user.profile.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "Контроллер профиля пользователя", description = "Операции с профилем текущего пользователя")
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final ProfileService profileService;

    @Operation(summary = "Получение профиля текущего пользователя")
    @ApiResponse(
            responseCode = "200",
            description = "Успешное получение профиля",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Profile.class)
            )

    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Profile getProfile(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileService.getByUsername(authentication.getName());
    }

    @Operation(
            summary = "Редактирование профиля текущего пользователя",
            description = "Операция позволяет редактировать выборочные данные путем включения в " +
                    "тело запроса только тех параметров, которые должны измениться"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Профиль успешно изменен"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации тела запроса",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Новый email уже существует",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @PutMapping(value = "/edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void editProfile(
            @Parameter(name = "Новые данные профиля", required = true)
            @RequestBody @Valid ProfileUpdateRequest profileUpdateRequest
    ){
        //TODO remove edit, set only put method
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        profileService.update(authentication.getName(), profileUpdateRequest);
    }
}
