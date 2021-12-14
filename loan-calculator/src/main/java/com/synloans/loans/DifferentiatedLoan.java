package com.synloans.loans;

import com.synloans.loans.info.LoanInfo;
import com.synloans.loans.payment.LoanPayment;
import com.synloans.loans.payment.PaymentSum;
import lombok.NonNull;
import org.javamoney.moneta.Money;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DifferentiatedLoan implements Loan {
    @NotNull
    private final LoanInfo loanInfo;
    private final Money totalPayout;
    private final Money monthPrincipalPayment;
    @NotNull
    private final List<LoanPayment> payments;

    public DifferentiatedLoan(@NotNull LoanInfo loanInfo) {
        Objects.requireNonNull(loanInfo);
        this.loanInfo = loanInfo;
        this.monthPrincipalPayment = loanInfo.getLoanSum()
                .divide(loanInfo.getMonths());
        this.payments = createPaymentsList();
        this.totalPayout = calcTotalPayout();
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
        return getPaymentByMonthNumber(monthNumber)
                .getPaymentSum()
                .getAmount();
    }

    @Override
    public Money getTotalPayout() {
        return totalPayout;
    }

    @Override
    public Money getMonthlyCreditBalance(int monthNumber) {
        return payments.get(monthNumber - 1).getLoanBalance();
    }

    @Override
    public @NonNull Money getMonthlyPrincipalPayout(int monthNumber) {
        return monthPrincipalPayment;
    }

    @Override
    public Money getMonthlyPercentPayout(int monthNumber) {
        return getPaymentByMonthNumber(monthNumber)
                .getPaymentSum()
                .getPercentPart();
    }

    @Override
    public Money getTotalPercentPayout() {
        return totalPayout.subtract(loanInfo.getLoanSum());
    }


    private Money calcTotalPayout() {
        Money acc = Money.zero(loanInfo.getLoanSum().getCurrency());
        for (LoanPayment payment: payments){
            acc = acc.add(payment.getPaymentSum().getAmount());
        }
        return acc;
    }

    private List<LoanPayment> createPaymentsList() {
        List<LoanPayment> paymentsList = new ArrayList<>(loanInfo.getMonths());
        for (int monthIndex = 1; monthIndex <= this.loanInfo.getMonths(); ++monthIndex) {
            Money balance = loanInfo.getLoanSum()
                    .subtract(monthPrincipalPayment.multiply(monthIndex));
            paymentsList.add(new LoanPayment(
                    calcMonthlyPaymentSum(monthIndex),
                    balance,
                    loanInfo.getLoanDate().plusMonths(monthIndex)
            ));
        }
        return paymentsList;
    }

    private PaymentSum calcMonthlyPaymentSum(int monthNumber) {
        Money base = loanInfo.getLoanSum().divide(loanInfo.getMonths());
        BigDecimal monthlyRate = loanInfo.getRate().divide(BigDecimal.valueOf(12), 12, RoundingMode.CEILING);
        Money tmp = loanInfo.getLoanSum()
                .subtract(
                        base.multiply(monthNumber - 1L)
                );
        Money fullSum = base.add(
                tmp.multiply(monthlyRate)
        );
        Money percentPart = fullSum.subtract(monthPrincipalPayment);
        return new PaymentSum(monthPrincipalPayment, percentPart);
    }
}
