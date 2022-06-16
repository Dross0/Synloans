package com.synloans.loans.controller.impl;

import com.synloans.loans.configuration.api.ApiInfo;
import com.synloans.loans.controller.PaymentScheduleController;
import com.synloans.loans.exception.advice.response.ErrorResponse;
import com.synloans.loans.model.LoanTerms;
import com.synloans.loans.model.payment.Payment;
import com.synloans.loans.service.calculator.impl.AnnuityPaymentScheduleCalculator;
import com.synloans.loans.service.calculator.impl.DifferentiatedPaymentScheduleCalculator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "Контроллер графика платежей", description = "Расчет графика платежей по кредиту")
@RestController
@RequestMapping(ApiInfo.API_VERSION + "/schedule")
@RequiredArgsConstructor
@Slf4j
public class PaymentScheduleControllerImpl implements PaymentScheduleController {

    private final AnnuityPaymentScheduleCalculator annuityPaymentScheduleCalculator;

    private final DifferentiatedPaymentScheduleCalculator differentiatedPaymentScheduleCalculator;

    @Operation(summary = "Расчет графика платежей с аннуитетом")
    @ApiResponse(
            responseCode = "200",
            description = "График платежей успешно расчитан",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Payment.class))
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации условий кредита",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @PostMapping(
            value = "/annuity",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public List<Payment> calculateAnnuityPaymentSchedule(@Valid @RequestBody LoanTerms loanTerms) {
        return annuityPaymentScheduleCalculator.calculateSchedule(loanTerms);
    }


    @Operation(summary = "Расчет графика платежей с дифференцированными платежами")
    @ApiResponse(
            responseCode = "200",
            description = "График платежей успешно расчитан",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Payment.class))
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации условий кредита",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @PostMapping(
            value = "/differentiated",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public List<Payment> calculateDifferentiatedPaymentSchedule(@Valid @RequestBody LoanTerms loanTerms) {
        return differentiatedPaymentScheduleCalculator.calculateSchedule(loanTerms);
    }
}
