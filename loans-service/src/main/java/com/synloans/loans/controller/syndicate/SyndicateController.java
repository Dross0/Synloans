package com.synloans.loans.controller.syndicate;

import com.synloans.loans.model.dto.SyndicateJoinRequest;
import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.security.UserRole;
import com.synloans.loans.service.company.BankService;
import com.synloans.loans.service.exception.SyndicateJoinException;
import com.synloans.loans.service.exception.advice.response.ErrorResponse;
import com.synloans.loans.service.exception.notfound.BankNotFoundException;
import com.synloans.loans.service.syndicate.SyndicateParticipantService;
import com.synloans.loans.service.syndicate.SyndicateService;
import com.synloans.loans.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "Контроллер участников синдиката", description = "Управление участием в синдикате")
@RestController
@RequestMapping("/syndicates")
@RequiredArgsConstructor
@Slf4j
public class SyndicateController {
    private final UserService userService;
    private final BankService bankService;
    private final SyndicateService syndicateService;
    private final SyndicateParticipantService syndicateParticipantService;


    @Operation(summary = "Вступление банка в синдикат на заявку")
    @ApiResponse(
            responseCode = "200",
            description = "Участник успешно зарегистрировался"
    )
    @ApiResponse(
            responseCode = "500",
            description = "Ошибка при регистрации участника синдиката",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Не найдена заявка по id",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
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
            responseCode = "403",
            description = "Компания текущего пользователя не является банком"
    )
    @Secured(UserRole.ROLE_BANK)
    @PostMapping(value = "/join", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void joinTo(
            @RequestBody @Valid SyndicateJoinRequest joinRequest,
            Authentication authentication
    ){
        Bank bank = getBankByUsername(authentication);
        syndicateService.joinBankToSyndicate(joinRequest, bank)
                .orElseThrow(() ->
                        new SyndicateJoinException("Не удалось зарегистировать участника синдиката")
                );
    }


    @Operation(summary = "Выход банка из синдиката")
    @ApiResponse(
            responseCode = "200",
            description = "Участник успешно вышел из синдиката"
    )
    @ApiResponse(
            responseCode = "500",
            description = "Выход из синдиката, при выданном кредите",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Не найден банк текущего пользователя",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Компания текущего пользователя не является банком"
    )
    @Secured(UserRole.ROLE_BANK)
    @DeleteMapping(value = "/{loanRequestId}")
    public void quitFrom(
            @Parameter(name = "Id заявки из синдиката которой нужно выйти")
            @PathVariable("loanRequestId") Long id,
            Authentication authentication
    ){
        syndicateParticipantService.quitFromSyndicate(id,  getBankByUsername(authentication));
    }

    private Bank getBankByUsername(Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        Bank bank = bankService.getByCompany(user.getCompany());
        if (bank == null){
            log.error("Not found bank at user='{}' with company='{}'", user.getUsername(), user.getCompany().getFullName());
            throw new BankNotFoundException("Не найден банк пользователя");
        }
        return bank;
    }
}
