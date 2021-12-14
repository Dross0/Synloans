package com.synloans.loans.info;


import lombok.NonNull;
import lombok.Value;
import org.javamoney.moneta.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Value
public class LoanInfo {
    @NonNull Money loanSum;
    @NonNull BigDecimal rate;
    @NonNull LocalDate loanDate;
    int months;

    public LoanInfo(
            Money loanSum,
            BigDecimal rate,
            LocalDate loanDate,
            int months
    ) {
        this.loanSum = Objects.requireNonNull(loanSum, "Loan sum required");
        validateRate(rate);
        validateMonths(months);
        this.rate = rate;
        this.months = months;
        this.loanDate = Objects.requireNonNull(loanDate, "Loan date required");
    }

    private void validateMonths(int months) {
        if (months <= 0){
            throw new IllegalArgumentException("Число месяцев должно быть положительным");
        }
    }

    private void validateRate(BigDecimal rate){
        if (rate == null || rate.signum() == -1){
            throw new IllegalArgumentException("Процентрая ставка должна быть положительной");
        }
    }
}
