package com.synloans.loans.client;

import com.synloans.loans.model.schedule.LoanTerms;
import com.synloans.loans.model.schedule.Payment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "PaymentsCalculatorClient",
        url = "${payments-calculator.service.host}"
)
public interface PaymentsCalculatorClient {

    @PostMapping(
            value = "/schedule/annuity",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Payment> calculateAnnuityPaymentSchedule(@RequestBody LoanTerms loanTerms);

    @PostMapping(
            value = "/schedule/differentiated",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Payment> calculateDifferentiatedPaymentSchedule(@RequestBody LoanTerms loanTerms);

}
