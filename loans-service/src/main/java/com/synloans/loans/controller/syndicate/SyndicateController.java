package com.synloans.loans.controller.syndicate;

import com.synloans.loans.model.dto.SyndicateJoinRequest;
import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.security.UserRole;
import com.synloans.loans.service.company.BankService;
import com.synloans.loans.service.exception.SyndicateJoinException;
import com.synloans.loans.service.exception.notfound.BankNotFoundException;
import com.synloans.loans.service.syndicate.SyndicateParticipantService;
import com.synloans.loans.service.syndicate.SyndicateService;
import com.synloans.loans.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/syndicates")
@RequiredArgsConstructor
@Slf4j
public class SyndicateController {
    private final UserService userService;
    private final BankService bankService;
    private final SyndicateService syndicateService;
    private final SyndicateParticipantService syndicateParticipantService;


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

    @Secured(UserRole.ROLE_BANK)
    @DeleteMapping(value = "/{loanRequestId}")
    public void quitFrom(@PathVariable("loanRequestId") Long id, Authentication authentication){
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
