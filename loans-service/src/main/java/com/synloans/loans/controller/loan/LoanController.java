package com.synloans.loans.controller.loan;

import com.synloans.loans.model.dto.loan.LoanCreateResponse;
import com.synloans.loans.model.dto.loan.payments.ActualPaymentDto;
import com.synloans.loans.model.dto.loan.payments.PaymentRequest;
import com.synloans.loans.model.dto.loan.payments.PlannedPaymentDto;
import com.synloans.loans.model.entity.loan.Loan;
import com.synloans.loans.model.entity.loan.payment.ActualPayment;
import com.synloans.loans.model.entity.loan.payment.PlannedPayment;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.service.loan.LoanService;
import com.synloans.loans.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    private final UserService userService;

    private final Converter<PlannedPayment, PlannedPaymentDto> plannedPaymentConverter;
    private final Converter<ActualPayment, ActualPaymentDto> actualPaymentConverter;

    @PostMapping(value = "/{loanRequestId}/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public LoanCreateResponse startLoan(
            @PathVariable("loanRequestId") long loanRequestId,
            Authentication authentication
    ){
        User currentUser = userService.getCurrentUser(authentication);
        Loan loan = loanService.startLoanByRequestId(loanRequestId, currentUser);
        return new LoanCreateResponse(loanRequestId, loan.getId());
    }


    //TODO Проверять причастность текущего пользователя к платежам
    @GetMapping(value = "/{loanRequestId}/payments/plan", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PlannedPaymentDto> getPlannedPayments(@PathVariable("loanRequestId") long loanRequestId){
        return loanService.getPlannedPaymentsByRequestId(loanRequestId)
                .stream()
                .map(plannedPaymentConverter::convert)
                .collect(Collectors.toList());

    }

    //TODO Нужно ли проверять причастность текущего пользователя к платежам
    @GetMapping(value = "/{loanRequestId}/payments/actual", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ActualPaymentDto> getActualPayments(@PathVariable("loanRequestId") long loanRequestId){
        return loanService.getActualPaymentsByRequestId(loanRequestId)
                .stream()
                .map(actualPaymentConverter::convert)
                .collect(Collectors.toList());
    }

    @PostMapping(value = "/{loanRequestId}/pay", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ActualPaymentDto acceptPayment(
            @PathVariable("loanRequestId") long loanRequestId,
            @RequestBody @Valid PaymentRequest paymentRequest,
            Authentication authentication
    ){
        User currentUser = userService.getCurrentUser(authentication);
        ActualPayment actualPayment = loanService.acceptPayment(loanRequestId, paymentRequest, currentUser);
        return actualPaymentConverter.convert(actualPayment);
    }

}
