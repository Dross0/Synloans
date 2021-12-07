package com.sinloans.loans.controller;

import com.sinloans.loans.model.dto.LoanRequestResponse;
import com.sinloans.loans.model.dto.LoanSum;
import com.sinloans.loans.model.entity.*;
import com.sinloans.loans.model.dto.LoanRequestDto;
import com.sinloans.loans.service.LoanRequestService;
import com.sinloans.loans.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/loan/requests")
@Slf4j
public class LoanRequestController {
    private final LoanRequestService loanRequestService;
    private final UserService userService;


    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createLoanRequest(@RequestBody LoanRequestDto loanRequestDto, Authentication authentication){
        User user = getCurrentUser(authentication);
        loanRequestService.createRequest(
                loanRequestDto,
                user.getCompany()
        );
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Collection<LoanRequestResponse> getCompanyRequests(Authentication authentication){
        User user = getCurrentUser(authentication);
        return buildCollectionResponse(user.getCompany().getLoanRequests());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public LoanRequestResponse getRequestById(@PathVariable("id") Long id, Authentication authentication){
        User user = getCurrentUser(authentication);
        Optional<LoanRequest> loanRequestOpt = loanRequestService.getById(id);
        if (loanRequestOpt.isEmpty()){
            log.error("Заявка на кредит с id={} не найдена", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Заявка на кредит с id=" + id + " не найдена");
        }
        LoanRequest loanRequest = loanRequestOpt.get();
        if (user.getCompany().getId().equals(loanRequest.getCompany().getId())){
            log.error("Заявка не принадлежит текущему пользователю");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Заявка на кредит с id=" + id + " не принадлежит пользователю=" + user.getUsername());
        }
        return buildResponse(loanRequest);
    }

    @GetMapping(value = "/{id}/participants", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Collection<SyndicateParticipant> getSyndicateParticipantsForRequest(@PathVariable("id") Long id){
        Optional<LoanRequest> loanRequest = loanRequestService.getById(id);
        if (loanRequest.isEmpty()){
            log.error("Заявка на кредит с id={} не найдена", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Заявка на кредит с id=" + id + " не найдена");
        }
        Syndicate syndicate = loanRequest.get().getSyndicate();
        if (syndicate == null){
            log.error("Синдикат на заявку с id={} не найден", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Синдикат на заявку с id=" + id + " не найден");
        }
        return syndicate.getParticipants();
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Collection<LoanRequestResponse> getAllRequests(){
        return buildCollectionResponse(loanRequestService.getAll());
    }

    @DeleteMapping(value = "/{id}")
    public void deleteRequest(@PathVariable("id") Long id){
        loanRequestService.deleteById(id);
    }

    private Collection<LoanRequestResponse> buildCollectionResponse(Collection<LoanRequest> loanRequests){
        return loanRequests.stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    private LoanRequestResponse buildResponse(LoanRequest loanRequest){
        LoanRequestResponse response = new LoanRequestResponse();
        response.setId(loanRequest.getId());
        response.setTerm(loanRequest.getTerm());
        response.setDateCreate(loanRequest.getCreateDate());
        response.setMaxRate(loanRequest.getRate());
        response.setDateIssue(null);
        Loan loan = loanRequest.getLoan();
        if (loan != null) {
            response.setDateIssue(loan.getRegistrationDate());
        }
        response.setSum(LoanSum.valueOf(loanRequest.getSum()));
        return response;
    }

    private User getCurrentUser(Authentication authentication){
        String username = authentication.getName();
        User curUser = userService.getUserByUsername(username);
        if (curUser == null){
            log.error("Не удалось найти текущего пользователя с username={}", username);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Не удалось получить текущего пользователя с username=" + username);
        }
        return curUser;
    }
}
