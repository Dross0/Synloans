package com.synloans.loans.service.calculator.impl;

import java.time.LocalDate;

public class ExpectedPayment {
    private final LocalDate date;
    private final double percent;
    private final double principal;
    private final double balance;

    public ExpectedPayment(LocalDate date, double percent, double principal, double balance) {
        this.date = date;
        this.percent = percent;
        this.principal = principal;
        this.balance = balance;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public double getPercent() {
        return this.percent;
    }

    public double getPrincipal() {
        return this.principal;
    }

    public double getBalance() {
        return this.balance;
    }
}
