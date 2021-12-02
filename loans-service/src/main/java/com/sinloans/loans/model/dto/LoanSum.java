package com.sinloans.loans.model.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoanSum {
    private int value;

    private SumUnit unit;

    public long getSum(){
        return (long) unit.getValue() * value;
    }
}
