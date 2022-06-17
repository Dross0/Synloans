package com.synloans.loans.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class LoanSum {
    private long value;

    private SumUnit unit;

    @JsonIgnore
    public long getSum(){
        return unit.getValue() * value;
    }

    public static LoanSum valueOf(long value){
        if (value < SumUnit.THOUSAND.getValue()){
            return null;
        } else if (value < SumUnit.MILLION.getValue()){
            return new LoanSum(value / SumUnit.THOUSAND.getValue(), SumUnit.THOUSAND);
        } else if (value < SumUnit.BILLION.getValue()){
            if (value % SumUnit.MILLION.getValue() != 0){
                return new LoanSum(value / SumUnit.THOUSAND.getValue(), SumUnit.THOUSAND);
            }
            return new LoanSum(value / SumUnit.MILLION.getValue(), SumUnit.MILLION);
        } else {
            if (value % SumUnit.BILLION.getValue() != 0){
                return new LoanSum(value / SumUnit.MILLION.getValue(), SumUnit.MILLION);
            }
            return new LoanSum(value / SumUnit.BILLION.getValue(), SumUnit.BILLION);
        }
    }
}
