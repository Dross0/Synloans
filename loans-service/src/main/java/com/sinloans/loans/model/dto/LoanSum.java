package com.sinloans.loans.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoanSum {
    private int value;

    private SumUnit unit;

    @JsonIgnore
    public long getSum(){
        return (long) unit.getValue() * value;
    }

    public static LoanSum valueOf(long value){
        if (value < SumUnit.THOUSAND.getValue()){
            return null;
        } else if (value < SumUnit.MILLION.getValue()){
            return new LoanSum((int) value / SumUnit.THOUSAND.getValue(), SumUnit.THOUSAND);
        } else if (value < SumUnit.BILLION.getValue()){
            if (value % SumUnit.MILLION.getValue() != 0){
                return new LoanSum((int) value / SumUnit.THOUSAND.getValue(), SumUnit.THOUSAND);
            }
            return new LoanSum((int) value / SumUnit.MILLION.getValue(), SumUnit.MILLION);
        } else {
            if (value % SumUnit.BILLION.getValue() != 0){
                return new LoanSum((int) value / SumUnit.MILLION.getValue(), SumUnit.MILLION);
            }
            return new LoanSum((int) value / SumUnit.BILLION.getValue(), SumUnit.BILLION);
        }
    }
}
