package com.synloans.loans.adapter.controller;

import com.synloans.loans.adapter.dto.BankJoinRequest;
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
@RequestMapping("/banks")
@RequiredArgsConstructor
@Slf4j
public class BankParticipantsController {

    private final LoanAdapterService loanService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void joinBank(@Valid @RequestBody BankJoinRequest bankJoinRequest){
        log.info("Bank with address='{}' join to loan with id = '{}'",
                bankJoinRequest.getBank().getAddress(),
                bankJoinRequest.getLoanId().getId()
        );
        loanService.joinBank(bankJoinRequest);
    }

}
