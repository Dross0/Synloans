package com.sinloans.loans.controller;

import com.sinloans.loans.model.dto.LoanRequestResponse;
import com.sinloans.loans.model.dto.LoanSum;
import com.sinloans.loans.model.entity.*;
import com.sinloans.loans.model.dto.LoanRequestDto;
import com.sinloans.loans.service.LoanRequestService;
import com.sinloans.loans.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
    public Collection<LoanRequestResponse> getCompanyRequests(Authentication authentication){
        String username = authentication.getName();
        User curUser = userService.getUserByUsername(username);
        if (curUser == null){
            throw new IllegalStateException("Не удалось получить текущего пользователя с username=" + username);
        }
        return buildResponse(curUser.getCompany().getLoanRequests());
    }

    @GetMapping(value = "/participants", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Collection<SyndicateParticipant> getSyndicateParticipantsForRequest(@RequestBody Long id){
        Optional<LoanRequest> loanRequest = loanRequestService.getById(id);
        if (loanRequest.isEmpty()){
            return Collections.emptyList(); //TODO maybe exception
        }
        Syndicate syndicate = loanRequest.get().getSyndicate();
        if (syndicate == null){
            return Collections.emptyList();
        }
        return syndicate.getParticipants();
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Collection<LoanRequestResponse> getAllRequests(){
        return buildResponse(loanRequestService.getAll());
    }

    @DeleteMapping(value = "/delete")
    public void deleteRequest(@RequestBody Long id){
        loanRequestService.deleteById(id);
    }

    private Collection<LoanRequestResponse> buildResponse(Collection<LoanRequest> loanRequests){
        List<LoanRequestResponse> responseList = new ArrayList<>(loanRequests.size());
        for (LoanRequest request: loanRequests){
            LoanRequestResponse response = new LoanRequestResponse();
            response.setId(request.getId());
            response.setTerm(request.getTerm());
            response.setDateCreate(request.getCreateDate());
            response.setMaxRate(request.getRate());
            response.setDateIssue(null);
            Loan loan = request.getLoan();
            if (loan != null) {
                response.setDateIssue(loan.getRegistrationDate());
            }
            response.setSum(LoanSum.valueOf(request.getSum()));
            responseList.add(response);
        }
        return responseList;
    }
}
