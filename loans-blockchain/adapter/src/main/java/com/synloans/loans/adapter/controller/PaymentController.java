package com.synloans.loans.adapter.controller;

import com.synloans.loans.adapter.dto.PaymentRequest;
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
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final LoanAdapterService loanService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void applyPayment(@Valid @RequestBody PaymentRequest paymentRequest){
        log.info("Add payment with sum='{}' by borrower with address='{}' to loan='{}'",
                paymentRequest.getPayment(),
                paymentRequest.getPayer().getAddress(),
                paymentRequest.getLoanId().getId()
        );
        loanService.payLoan(paymentRequest);
    }

}
