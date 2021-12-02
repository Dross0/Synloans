package com.sinloans.loans.model.dto;

public enum SumUnit {
    THOUSAND(1_000),
    MILLION(1_000_000),
    BILLION(1_000_000_000);

    private final int value;

    SumUnit(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
