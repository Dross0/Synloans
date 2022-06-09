package com.synloans.loans.info

import com.synloans.loans.model.info.LoanInfo
import org.javamoney.moneta.Money
import spock.lang.Specification

import java.time.LocalDate

class LoanInfoTest extends Specification{
    def "Тест. Создание информации о кредите"(){
        given:
            def sum = Money.of(100_000, "RUR")
            def rate = BigDecimal.valueOf(0.1)
            def months = 14
            def date = LocalDate.now()
        when:
            def loanInfo = new LoanInfo(sum, rate, date, months)
        then:
            noExceptionThrown()
            with(loanInfo){
                it.loanSum == sum
                it.rate == rate
                it.loanDate == date
                it.months == months
            }
    }

    def "Тест. Отрицательный процент при создании информации о кредите"(){
        given:
            def sum = Money.of(100_000, "RUR")
            def rate = BigDecimal.valueOf(-0.1)
            def months = 14
            def date = LocalDate.now()
        when:
            def loanInfo = new LoanInfo(sum, rate, date, months)
        then:
            thrown(IllegalArgumentException)
    }

    def "Тест. Неверное число месяцев при создании информации о кредите"(){
        given:
            def sum = Money.of(100_000, "RUR")
            def rate = BigDecimal.valueOf(0.1)
            def date = LocalDate.now()
        when:
            def loanInfo = new LoanInfo(sum, rate, date, months)
        then:
            thrown(IllegalArgumentException)
        where:
            months << [-13, 0]
    }
}
