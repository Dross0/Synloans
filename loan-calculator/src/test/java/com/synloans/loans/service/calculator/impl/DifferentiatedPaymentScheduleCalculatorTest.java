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
        DifferentiatedPaymentScheduleCalculator.class
})
class DifferentiatedPaymentScheduleCalculatorTest {

    @Autowired
    PaymentScheduleCalculator paymentScheduleCalculator;

    @MockBean
    MonetaryOperator roundOperator;


    @Test
    @DisplayName("Расчет диффиренцированных платежей")
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
                        4166.67,
                        45833.33
                ),
                new ExpectedPayment(
                        LocalDate.of(2022, Month.AUGUST, 15),
                        840.28,
                        4166.67,
                        41666.67
                ),
                new ExpectedPayment(
                        LocalDate.of(2022, Month.SEPTEMBER, 15),
                        763.89,
                        4166.67,
                        37500
                ),
                new ExpectedPayment(
                        LocalDate.of(2022, Month.OCTOBER, 15),
                        687.5,
                        4166.67,
                        33333.33
                ),
                new ExpectedPayment(
                        LocalDate.of(2022, Month.NOVEMBER, 15),
                        611.11,
                        4166.67,
                        29166.67
                ),
                new ExpectedPayment(
                        LocalDate.of(2022, Month.DECEMBER, 15),
                        534.72,
                        4166.67,
                        25000
                ),
                new ExpectedPayment(
                        LocalDate.of(2023, Month.JANUARY, 15),
                        458.33,
                        4166.67,
                        20833.33
                ),
                new ExpectedPayment(
                        LocalDate.of(2023, Month.FEBRUARY, 15),
                        381.94,
                        4166.67,
                        16666.67
                ),
                new ExpectedPayment(
                        LocalDate.of(2023, Month.MARCH, 15),
                        305.56,
                        4166.67,
                        12500
                ),
                new ExpectedPayment(
                        LocalDate.of(2023, Month.APRIL, 15),
                        229.17,
                        4166.67,
                        8333.33
                ),
                new ExpectedPayment(
                        LocalDate.of(2023, Month.MAY, 15),
                        152.78,
                        4166.67,
                        4166.67
                ),
                new ExpectedPayment(
                        LocalDate.of(2023, Month.JUNE, 15),
                        76.39,
                        4166.67,
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