package com.synloans.loans.service.calculator.impl;

import com.synloans.loans.model.LoanTerms;
import com.synloans.loans.model.payment.Payment;
import com.synloans.loans.model.payment.PaymentSum;
import com.synloans.loans.service.calculator.PaymentScheduleCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.money.MonetaryAmount;
import javax.money.MonetaryOperator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DifferentiatedPaymentScheduleCalculator implements PaymentScheduleCalculator {

    private final MonetaryOperator roundOperator;

    @Override
    public List<Payment> calculateSchedule(LoanTerms loanTerms) {
        MonetaryAmount monthPrincipalPayment = loanTerms.getLoanSum()
                .divide(loanTerms.getMonths());
        List<Payment> paymentsList = new ArrayList<>(loanTerms.getMonths());
        for (int monthIndex = 1; monthIndex <= loanTerms.getMonths(); ++monthIndex) {
            MonetaryAmount balance = loanTerms.getLoanSum()
                    .subtract(monthPrincipalPayment.multiply(monthIndex));
            paymentsList.add(new Payment(
                    calcMonthlyPaymentSum(loanTerms, monthPrincipalPayment, monthIndex),
                    roundOperator.apply(balance),
                    loanTerms.getIssueDate().plusMonths(monthIndex)
            ));
        }
        return paymentsList;
    }

    private PaymentSum calcMonthlyPaymentSum(LoanTerms loanTerms, MonetaryAmount monthPrincipalPayment, int monthNumber) {
        MonetaryAmount base = loanTerms.getLoanSum().divide(loanTerms.getMonths());
        BigDecimal monthlyRate = loanTerms.getRate().divide(BigDecimal.valueOf(12), 12, RoundingMode.CEILING);
        MonetaryAmount tmp = loanTerms.getLoanSum()
                .subtract(
                        base.multiply(monthNumber - 1L)
                );
        MonetaryAmount fullSum = base.add(
                tmp.multiply(monthlyRate)
        );
        MonetaryAmount percentPart = fullSum.subtract(monthPrincipalPayment);
        return new PaymentSum(
                roundOperator.apply(monthPrincipalPayment),
                roundOperator.apply(percentPart)
        );
    }
}
