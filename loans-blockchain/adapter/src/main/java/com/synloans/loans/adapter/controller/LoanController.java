package com.synloans.loans.adapter.controller;

import com.synloans.loans.adapter.dto.LoanCreateRequest;
import com.synloans.loans.adapter.dto.LoanId;
import com.synloans.loans.adapter.service.LoanAdapterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
@Slf4j
public class LoanController {

    private final LoanAdapterService loanService;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public LoanId createLoan(@Valid @RequestBody LoanCreateRequest loanCreateRequest){
        log.info("Create loan request with bank agent at address={} by borrower={}",
                loanCreateRequest.getBankAgent().getAddress(),
                loanCreateRequest.getBorrower()
        );
        return loanService.createLoan(loanCreateRequest);
    }

}
