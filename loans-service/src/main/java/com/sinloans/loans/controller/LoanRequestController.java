package com.sinloans.loans.controller;

import com.sinloans.loans.model.LoanRequest;
import com.sinloans.loans.service.LoanRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loan/request")
@RequiredArgsConstructor
public class LoanRequestController {
    private final LoanRequestService requestService;


    @GetMapping("/list")
    public List<LoanRequest> loanRequestList(@RequestParam(value = "limit", required = false) Long limit){
        return requestService.getListOfRequests(limit);
    }

    @GetMapping("/{id}")
    public LoanRequest loanRequest(@PathVariable("id") Long id){
        return requestService.getById(id);
    }
}
