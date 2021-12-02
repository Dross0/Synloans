package com.sinloans.loans.controller;

import com.sinloans.loans.model.entity.LoanRequest;
import com.sinloans.loans.model.entity.User;
import com.sinloans.loans.model.dto.LoanRequestDto;
import com.sinloans.loans.service.LoanRequestService;
import com.sinloans.loans.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/loan/request")
public class LoanRequestController {
    private final LoanRequestService loanRequestService;
    private final UserService userService;


    @PostMapping(value = "/new", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createLoanRequest(@RequestBody LoanRequestDto loanRequestDto, Authentication authentication){
        String username = authentication.getName();
        User curUser = userService.getUserByUsername(username);
        if (curUser == null){
            throw new IllegalStateException("Не удалось получить текущего пользователя с username=" + username);
        }
        loanRequestService.createRequest(loanRequestDto, curUser.getCompany());
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Collection<LoanRequest> getCompanyRequests(Authentication authentication){
        String username = authentication.getName();
        User curUser = userService.getUserByUsername(username);
        if (curUser == null){
            throw new IllegalStateException("Не удалось получить текущего пользователя с username=" + username);
        }
        return curUser.getCompany().getLoanRequests();
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Collection<LoanRequest> getAllRequests(){
        return loanRequestService.getAll();
    }
}
