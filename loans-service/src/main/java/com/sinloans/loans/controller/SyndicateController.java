package com.sinloans.loans.controller;

import com.sinloans.loans.model.dto.SyndicateJoinRequest;
import com.sinloans.loans.model.entity.Bank;
import com.sinloans.loans.model.entity.Syndicate;
import com.sinloans.loans.model.entity.SyndicateParticipant;
import com.sinloans.loans.model.entity.User;
import com.sinloans.loans.security.UserRole;
import com.sinloans.loans.service.BankService;
import com.sinloans.loans.service.SyndicateParticipantService;
import com.sinloans.loans.service.SyndicateService;
import com.sinloans.loans.service.UserService;
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
        Syndicate syndicate = syndicateService.getByLoanRequestId(joinRequest.getRequestId(), true);
        if (syndicate == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Не удалось получить/создать синдикат по заявке с id=" + joinRequest.getRequestId());
        }
        SyndicateParticipant participant = syndicateParticipantService.createNewParticipant(
                syndicate,
                bank,
                joinRequest.getSum().getSum(),
                joinRequest.isApproveBankAgent()
        );
        if (participant == null){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось зарегистировать участника синдиката");
        }
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
