package com.synloans.loans.model.dto

import spock.lang.Specification

class LoanSumTest extends Specification{
    def "Тест. Получение полного значения суммы"(){
        given:
            def loanSum = new LoanSum(102, sumUnit)
        expect:
            loanSum.getSum() == result
        where:
            sumUnit          || result
            SumUnit.THOUSAND || 102_000
            SumUnit.MILLION  || 102_000_000
            SumUnit.BILLION  || 102_000_000_000
    }


    def "Тест. Расчет LoanSum по значению"(){
        expect:
            LoanSum.valueOf(value) == loanSum
        where:
            value           || loanSum
            999             || null
            304_000         || new LoanSum(304, SumUnit.THOUSAND)
            144_999         || new LoanSum(144, SumUnit.THOUSAND)
            400_000_000     || new LoanSum(400, SumUnit.MILLION)
            400_850_000     || new LoanSum(400_850, SumUnit.THOUSAND)
            513_000_000_000 || new LoanSum(513, SumUnit.BILLION)
            513_130_000_000 || new LoanSum(513_130, SumUnit.MILLION)
    }
}
