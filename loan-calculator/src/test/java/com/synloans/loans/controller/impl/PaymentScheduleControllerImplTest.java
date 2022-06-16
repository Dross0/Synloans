package com.synloans.loans.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synloans.loans.configuration.api.ApiInfo;
import com.synloans.loans.configuration.mapper.ObjectMapperConfiguration;
import com.synloans.loans.model.LoanTerms;
import com.synloans.loans.model.payment.Payment;
import com.synloans.loans.model.payment.PaymentSum;
import com.synloans.loans.service.calculator.impl.AnnuityPaymentScheduleCalculator;
import com.synloans.loans.service.calculator.impl.DifferentiatedPaymentScheduleCalculator;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(ObjectMapperConfiguration.class)
@WebMvcTest(PaymentScheduleControllerImpl.class)
class PaymentScheduleControllerImplTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AnnuityPaymentScheduleCalculator annuityPaymentScheduleCalculator;

    @MockBean
    DifferentiatedPaymentScheduleCalculator differentiatedPaymentScheduleCalculator;

    private static Stream<Arguments> notValidLoanTerms() {
        return Stream.of(
                Arguments.of(
                        new LoanTerms(
                                null,
                                BigDecimal.valueOf(0.12),
                                LocalDate.of(2022, Month.JUNE, 15),
                                9
                        )
                ),
                Arguments.of(
                        new LoanTerms(
                                Money.of(BigDecimal.valueOf(50_000), "RUR"),
                                BigDecimal.valueOf(-0.12),
                                LocalDate.of(2022, Month.JUNE, 15),
                                9
                        )
                ),
                Arguments.of(
                        new LoanTerms(
                                Money.of(BigDecimal.valueOf(50_000), "RUR"),
                                BigDecimal.valueOf(0.12),
                                null,
                                9
                        )
                ),
                Arguments.of(
                        new LoanTerms(
                                Money.of(BigDecimal.valueOf(50_000), "RUR"),
                                BigDecimal.valueOf(0.12),
                                LocalDate.of(2022, Month.JUNE, 15),
                                -13
                        )
                ),
                Arguments.of(
                        new LoanTerms(
                                Money.of(BigDecimal.valueOf(-1000), "RUR"),
                                BigDecimal.valueOf(0.12),
                                LocalDate.of(2022, Month.JUNE, 15),
                                13
                        )
                )
        );
    }

    @Test
    @DisplayName("Получение аннуитентных платежей")
    void annuityPaymentScheduleTest() throws Exception {
        LoanTerms loanTerms = new LoanTerms(
                Money.of(BigDecimal.valueOf(50_000), "RUR"),
                BigDecimal.valueOf(0.12),
                LocalDate.of(2022, Month.JUNE, 15),
                9
        );

        Payment firstPayment = new Payment(
                new PaymentSum(
                        Money.of(BigDecimal.valueOf(1201.1), "RUR"),
                        Money.of(BigDecimal.valueOf(321.13), "RUR")
                ),
                Money.of(BigDecimal.valueOf(11122.03), "RUR"),
                LocalDate.of(2022, Month.JULY, 15)
        );
        Payment secondPayment = new Payment(
                new PaymentSum(
                        Money.of(BigDecimal.valueOf(457), "RUR"),
                        Money.of(BigDecimal.valueOf(144.5), "RUR")
                ),
                Money.of(BigDecimal.valueOf(1123.03), "RUR"),
                LocalDate.of(2022, Month.AUGUST, 16)
        );

        when(annuityPaymentScheduleCalculator.calculateSchedule(loanTerms))
                .thenReturn(List.of(firstPayment, secondPayment));

        mockMvc.perform(
                post(ApiInfo.API_VERSION + "/schedule/annuity")
                        .content(asJsonString(loanTerms))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$", hasSize(2)),
                        jsonPath("$[0].paymentSum.principalPart.amount", is(1201.1)),
                        jsonPath("$[0].paymentSum.principalPart.currency", is("RUR")),
                        jsonPath("$[0].paymentSum.percentPart.amount", is(321.13)),
                        jsonPath("$[0].paymentSum.percentPart.currency", is("RUR")),
                        jsonPath("$[0].loanBalance.amount", is(11122.03)),
                        jsonPath("$[0].loanBalance.currency", is("RUR")),
                        jsonPath("$[0].date", is("2022-07-15")),
                        jsonPath("$[1].paymentSum.principalPart.amount", is(457.0)),
                        jsonPath("$[1].paymentSum.principalPart.currency", is("RUR")),
                        jsonPath("$[1].paymentSum.percentPart.amount", is(144.5)),
                        jsonPath("$[1].paymentSum.percentPart.currency", is("RUR")),
                        jsonPath("$[1].loanBalance.amount", is(1123.03)),
                        jsonPath("$[1].loanBalance.currency", is("RUR")),
                        jsonPath("$[1].date", is("2022-08-16"))
                );
    }

    @ParameterizedTest
    @MethodSource("notValidLoanTerms")
    @DisplayName("Получение аннуитентных платежей с ошибкой валидации")
    void annuityPaymentScheduleWithValidationErrorTest(LoanTerms loanTerms) throws Exception {
        Payment firstPayment = new Payment(
                new PaymentSum(
                        Money.of(BigDecimal.valueOf(1201.1), "RUR"),
                        Money.of(BigDecimal.valueOf(321.13), "RUR")
                ),
                Money.of(BigDecimal.valueOf(11122.03), "RUR"),
                LocalDate.of(2022, Month.JULY, 15)
        );
        Payment secondPayment = new Payment(
                new PaymentSum(
                        Money.of(BigDecimal.valueOf(457), "RUR"),
                        Money.of(BigDecimal.valueOf(144.5), "RUR")
                ),
                Money.of(BigDecimal.valueOf(1123.03), "RUR"),
                LocalDate.of(2022, Month.AUGUST, 16)
        );

        when(annuityPaymentScheduleCalculator.calculateSchedule(loanTerms))
                .thenReturn(List.of(firstPayment, secondPayment));

        mockMvc.perform(
                post(ApiInfo.API_VERSION + "/schedule/annuity")
                        .content(asJsonString(loanTerms))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.code", is(400)),
                        jsonPath("$.status", is("BAD_REQUEST")),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").exists(),
                        jsonPath("$.timestamp").exists()
                );
    }

    @Test
    @DisplayName("Получение дифференцированных платежей")
    void differentiatedPaymentScheduleTest() throws Exception {
        LoanTerms loanTerms = new LoanTerms(
                Money.of(BigDecimal.valueOf(50_000), "RUR"),
                BigDecimal.valueOf(0.12),
                LocalDate.of(2022, Month.JUNE, 15),
                9
        );
        Payment firstPayment = new Payment(
                new PaymentSum(
                        Money.of(BigDecimal.valueOf(1201.1), "RUR"),
                        Money.of(BigDecimal.valueOf(321.13), "RUR")
                ),
                Money.of(BigDecimal.valueOf(11122.03), "RUR"),
                LocalDate.of(2022, Month.JULY, 15)
        );
        Payment secondPayment = new Payment(
                new PaymentSum(
                        Money.of(BigDecimal.valueOf(457), "RUR"),
                        Money.of(BigDecimal.valueOf(144.5), "RUR")
                ),
                Money.of(BigDecimal.valueOf(1123.03), "RUR"),
                LocalDate.of(2022, Month.AUGUST, 16)
        );

        when(differentiatedPaymentScheduleCalculator.calculateSchedule(loanTerms))
                .thenReturn(List.of(firstPayment, secondPayment));

        mockMvc.perform(
                post(ApiInfo.API_VERSION + "/schedule/differentiated")
                        .content(asJsonString(loanTerms))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$", hasSize(2)),
                        jsonPath("$[0].paymentSum.principalPart.amount", is(1201.1)),
                        jsonPath("$[0].paymentSum.principalPart.currency", is("RUR")),
                        jsonPath("$[0].paymentSum.percentPart.amount", is(321.13)),
                        jsonPath("$[0].paymentSum.percentPart.currency", is("RUR")),
                        jsonPath("$[0].loanBalance.amount", is(11122.03)),
                        jsonPath("$[0].loanBalance.currency", is("RUR")),
                        jsonPath("$[0].date", is("2022-07-15")),
                        jsonPath("$[1].paymentSum.principalPart.amount", is(457.0)),
                        jsonPath("$[1].paymentSum.principalPart.currency", is("RUR")),
                        jsonPath("$[1].paymentSum.percentPart.amount", is(144.5)),
                        jsonPath("$[1].paymentSum.percentPart.currency", is("RUR")),
                        jsonPath("$[1].loanBalance.amount", is(1123.03)),
                        jsonPath("$[1].loanBalance.currency", is("RUR")),
                        jsonPath("$[1].date", is("2022-08-16"))
                );
    }

    @ParameterizedTest
    @MethodSource("notValidLoanTerms")
    @DisplayName("Получение дифференцированных платежей с ошибкой валидации")
    void differentiatedPaymentScheduleWithValidationErrorTest(LoanTerms loanTerms) throws Exception {
        Payment firstPayment = new Payment(
                new PaymentSum(
                        Money.of(BigDecimal.valueOf(1201.1), "RUR"),
                        Money.of(BigDecimal.valueOf(321.13), "RUR")
                ),
                Money.of(BigDecimal.valueOf(11122.03), "RUR"),
                LocalDate.of(2022, Month.JULY, 15)
        );
        Payment secondPayment = new Payment(
                new PaymentSum(
                        Money.of(BigDecimal.valueOf(457), "RUR"),
                        Money.of(BigDecimal.valueOf(144.5), "RUR")
                ),
                Money.of(BigDecimal.valueOf(1123.03), "RUR"),
                LocalDate.of(2022, Month.AUGUST, 16)
        );

        when(annuityPaymentScheduleCalculator.calculateSchedule(loanTerms))
                .thenReturn(List.of(firstPayment, secondPayment));

        mockMvc.perform(
                post(ApiInfo.API_VERSION + "/schedule/differentiated")
                        .content(asJsonString(loanTerms))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.code", is(400)),
                        jsonPath("$.status", is("BAD_REQUEST")),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").exists(),
                        jsonPath("$.timestamp").exists()
                );
    }

    String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}