package com.synloans.loans;


import com.synloans.loans.info.LoanInfo;
import com.synloans.loans.payment.LoanPayment;
import com.synloans.loans.payment.PaymentSum;
import org.javamoney.moneta.Money;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AnnuityLoan implements Loan {
    @NotNull
    private final LoanInfo loanInfo;
    private final Money monthlyPayment;
    @NotNull
    private final List<LoanPayment> payments;

    public AnnuityLoan(@NotNull LoanInfo loanInfo) {
        this.loanInfo = Objects.requireNonNull(loanInfo);
        this.monthlyPayment = calcMonthlyPayment();
        this.payments = createPayments();
    }


    @Override
    @NotNull
    public LoanPayment getPaymentByMonthNumber(int monthNum) {
        return payments.get(monthNum - 1);
    }

    @Override
    @NotNull
    public List<LoanPayment> getPaymentsList() {
        return payments;
    }

    @Override
    @NotNull
    public LoanInfo getInfo() {
        return loanInfo;
    }

    @Override
    public Money getMonthlyPayment(int monthNumber) {
        return monthlyPayment;
    }

    @Override
    public Money getTotalPayout() {
        return monthlyPayment.multiply(loanInfo.getMonths());
    }

    @Override
    public Money getMonthlyCreditBalance(int monthNumber) {
        return payments.get(monthNumber - 1).getLoanBalance();
    }

    @Override
    public Money getMonthlyPrincipalPayout(int monthNumber) {
        return payments.get(monthNumber - 1)
                .getPaymentSum()
                .getPrincipalPart();
    }

    @Override
    public Money getMonthlyPercentPayout(int monthNumber) {
        return payments.get(monthNumber - 1)
                .getPaymentSum()
                .getPercentPart();
    }

    @Override
    public Money getTotalPercentPayout() {
        return getTotalPayout().subtract(loanInfo.getLoanSum());
    }

    private PaymentSum getPaymentSum(int monthNumber) {
        Money lastSum = loanInfo.getLoanSum();
        BigDecimal monthPercent = loanInfo.getRate()
                .divide(BigDecimal.valueOf(12), 12, RoundingMode.CEILING);
        Money percentPayout = Money.zero(lastSum.getCurrency());
        Money principalPayout = Money.zero(lastSum.getCurrency());
        for (int month = 1; month <= monthNumber; ++month) {
            percentPayout = lastSum.multiply(monthPercent);
            principalPayout = monthlyPayment.subtract(percentPayout);
            lastSum = lastSum.subtract(principalPayout);
        }
        return new PaymentSum(principalPayout, percentPayout);
    }

    private Money calcMonthlyPayment() {
        BigDecimal monthlyRate = loanInfo.getRate().divide(BigDecimal.valueOf(12), 12, RoundingMode.CEILING);
        return loanInfo.getLoanSum()
                .multiply(calcAnnuityCoefficient(monthlyRate));
    }

    private BigDecimal calcAnnuityCoefficient(BigDecimal monthlyRate){
        BigDecimal tmp = monthlyRate
                .add(BigDecimal.ONE)
                .pow(loanInfo.getMonths());
        BigDecimal numerator = monthlyRate.multiply(tmp);
        BigDecimal denominator = tmp.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 12, RoundingMode.CEILING);
    }

    private List<LoanPayment> createPayments() {
        List<LoanPayment> paymentsList = new ArrayList<>(loanInfo.getMonths());
        Money creditBalance = loanInfo.getLoanSum();
        for (int monthIndex = 1; monthIndex <= this.loanInfo.getMonths(); ++monthIndex) {
            PaymentSum paymentSum = getPaymentSum(monthIndex);
            creditBalance = creditBalance.subtract(paymentSum.getPrincipalPart());
            paymentsList.add(new LoanPayment(
                    paymentSum,
                    creditBalance,
                    loanInfo.getLoanDate().plusMonths(monthIndex)
            ));
        }
        return paymentsList;
    }
}
