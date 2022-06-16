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
public class AnnuityPaymentScheduleCalculator implements PaymentScheduleCalculator {

    private final MonetaryOperator roundOperator;

    @Override
    public List<Payment> calculateSchedule(LoanTerms loanTerms) {
        List<Payment> paymentsList = new ArrayList<>(loanTerms.getMonths());
        MonetaryAmount creditBalance = loanTerms.getLoanSum();
        BigDecimal annuityCoefficient = calculateAnnuityCoefficient(loanTerms);
        MonetaryAmount monthlyPayment = calculateMonthlyPayment(loanTerms, annuityCoefficient);
        for (int monthIndex = 1; monthIndex <= loanTerms.getMonths(); ++monthIndex) {
            PaymentSum paymentSum = calculateMonthPaymentSum(loanTerms, monthlyPayment, creditBalance);
            creditBalance = creditBalance.subtract(paymentSum.getPrincipalPart());
            paymentsList.add(new Payment(
                    new PaymentSum(
                            roundOperator.apply(paymentSum.getPrincipalPart()),
                            roundOperator.apply(paymentSum.getPercentPart())
                    ),
                    roundOperator.apply(creditBalance),
                    loanTerms.getIssueDate().plusMonths(monthIndex)
            ));
        }
        return paymentsList;
    }

    private PaymentSum calculateMonthPaymentSum(LoanTerms loanTerms, MonetaryAmount monthlyPayment, MonetaryAmount balance) {
        BigDecimal monthPercent = loanTerms.getRate()
                .divide(
                        BigDecimal.valueOf(12),
                        12,
                        RoundingMode.CEILING
                );

        MonetaryAmount percentPayout = balance.multiply(monthPercent);
        return new PaymentSum(
                monthlyPayment.subtract(percentPayout),
                percentPayout
        );
    }

    private MonetaryAmount calculateMonthlyPayment(LoanTerms loanTerms, BigDecimal annuityCoefficient) {
        return loanTerms.getLoanSum().multiply(annuityCoefficient);
    }

    private BigDecimal calculateAnnuityCoefficient(LoanTerms loanTerms){
        BigDecimal monthlyRate = loanTerms.getRate()
                .divide(
                        BigDecimal.valueOf(12),
                        12,
                        RoundingMode.CEILING
                );
        BigDecimal tmp = monthlyRate
                .add(BigDecimal.ONE)
                .pow(loanTerms.getMonths());
        BigDecimal numerator = monthlyRate.multiply(tmp);
        BigDecimal denominator = tmp.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 12, RoundingMode.CEILING);
    }


}
