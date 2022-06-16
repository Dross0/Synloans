package com.synloans.loans.service.calculator.impl;

import com.synloans.loans.model.LoanTerms;
import com.synloans.loans.model.payment.Payment;
import com.synloans.loans.service.calculator.PaymentScheduleCalculator;
import org.assertj.core.data.Offset;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.money.MonetaryAmount;
import javax.money.MonetaryOperator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        AnnuityPaymentScheduleCalculator.class
})
class AnnuityPaymentScheduleCalculatorTest {

    @Autowired
    PaymentScheduleCalculator paymentScheduleCalculator;

    @MockBean
    MonetaryOperator roundOperator;


    @Test
    @DisplayName("Расчет платежей аннуитет")
    void calculatePaymentsTest(){
        LocalDate date = LocalDate.of(2022, Month.JUNE, 15);
        LoanTerms loanTerms = new LoanTerms(
                Money.of(50_000, "RUR"),
                BigDecimal.valueOf(0.22),
                date,
                12
        );

        when(roundOperator.apply(any(MonetaryAmount.class))).thenAnswer(returnsFirstArg());

        List<ExpectedPayment> expectedPayments = List.of(
                new ExpectedPayment(
                        LocalDate.of(2022, Month.JULY, 15),
                        916.67,
                        3763.05,
                        46236.95
                ),
                new ExpectedPayment(
                        LocalDate.of(2022, Month.AUGUST, 15),
                        847.68,
                        3832.04,
                        42404.91
                ),
                new ExpectedPayment(
                        LocalDate.of(2022, Month.SEPTEMBER, 15),
                        777.42,
                        3902.3,
                        38502.61
                ),
                new ExpectedPayment(
                        LocalDate.of(2022, Month.OCTOBER, 15),
                        705.88,
                        3973.84,
                        34528.77
                ),
                new ExpectedPayment(
                        LocalDate.of(2022, Month.NOVEMBER, 15),
                        633.03,
                        4046.69,
                        30482.08
                ),
                new ExpectedPayment(
                        LocalDate.of(2022, Month.DECEMBER, 15),
                        558.84,
                        4120.88,
                        26361.2
                ),
                new ExpectedPayment(
                        LocalDate.of(2023, Month.JANUARY, 15),
                        483.29,
                        4196.43,
                        22164.77
                ),
                new ExpectedPayment(
                        LocalDate.of(2023, Month.FEBRUARY, 15),
                        406.35,
                        4273.36,
                        17891.41
                ),
                new ExpectedPayment(
                        LocalDate.of(2023, Month.MARCH, 15),
                        328.01,
                        4351.71,
                        13539.7
                ),
                new ExpectedPayment(
                        LocalDate.of(2023, Month.APRIL, 15),
                        248.23,
                        4431.49,
                        9108.2
                ),
                new ExpectedPayment(
                        LocalDate.of(2023, Month.MAY, 15),
                        166.98,
                        4512.74,
                        4595.47
                ),
                new ExpectedPayment(
                        LocalDate.of(2023, Month.JUNE, 15),
                        84.25,
                        4595.47,
                        0
                )
        );

        List<Payment> payments = paymentScheduleCalculator.calculateSchedule(loanTerms);

        assertThat(payments).hasSize(12);

        for (int i = 0; i < payments.size(); i++){
            assertThat(payments.get(i).getDate()).isEqualTo(expectedPayments.get(i).getDate());
            assertThat(payments.get(i).getPaymentSum().getPercentPart().getNumber().doubleValueExact())
                    .isCloseTo(expectedPayments.get(i).getPercent(), Offset.offset(0.01));
            assertThat(payments.get(i).getPaymentSum().getPrincipalPart().getNumber().doubleValueExact())
                    .isCloseTo(expectedPayments.get(i).getPrincipal(), Offset.offset(0.01));
            assertThat(payments.get(i).getLoanBalance().getNumber().doubleValueExact())
                    .isCloseTo(expectedPayments.get(i).getBalance(), Offset.offset(0.01));
        }
    }
}