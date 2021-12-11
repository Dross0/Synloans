package com.synloans.loans.controller;

import com.synloans.loans.model.dto.SyndicateJoinRequest;
import com.synloans.loans.model.entity.Bank;
import com.synloans.loans.model.entity.User;
import com.synloans.loans.security.UserRole;
import com.synloans.loans.service.company.BankService;
import com.synloans.loans.service.syndicate.SyndicateParticipantService;
import com.synloans.loans.service.syndicate.SyndicateService;
import com.synloans.loans.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/syndicates")
@RequiredArgsConstructor
public class SyndicateController {
    private final UserService userService;
    private final BankService bankService;
    private final SyndicateService syndicateService;
    private final SyndicateParticipantService syndicateParticipantService;


    @Secured(UserRole.ROLE_BANK)
    @PostMapping(value = "/join", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void joinTo(@RequestBody SyndicateJoinRequest joinRequest, Authentication authentication){
        Bank bank = getBankByUsername(authentication);
        syndicateService.joinBankToSyndicate(joinRequest, bank)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось зарегистировать участника синдиката")
                );
    }

    @Secured(UserRole.ROLE_BANK)
    @DeleteMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void quitFrom(@PathVariable("id") Long id, Authentication authentication){
        syndicateParticipantService.quitFromSyndicate(id,  getBankByUsername(authentication));
    }

    private Bank getBankByUsername(Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        if (user == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        Bank bank = bankService.getByCompany(user.getCompany());
        if (bank == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Не найден банк пользователя");
        }
        return bank;
    }
}
