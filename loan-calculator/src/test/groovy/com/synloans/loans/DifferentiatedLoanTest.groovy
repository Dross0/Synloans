package com.synloans.loans


import com.synloans.loans.info.LoanInfo
import org.javamoney.moneta.Money
import spock.lang.Specification

import java.time.LocalDate

import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.expect

class DifferentiatedLoanTest extends Specification{
    def "Тест. Создание кредита"() {
        given:
            def loanInfo = new LoanInfo(
                    Money.of(1000, "RUR"),
                    BigDecimal.valueOf(0.1),
                    LocalDate.now(),
                    12
            )
        when:
            def loan = new DifferentiatedLoan(loanInfo)
        then:
            loan.getInfo() == loanInfo
            noExceptionThrown()
    }


    def "Тест. Получение платежа в месяц"(){
        given:
            def loanInfo = new LoanInfo(
                    Money.of(128_304, "RUR"),
                    BigDecimal.valueOf(0.098),
                    LocalDate.now(),
                    4
            )
            def monthlyPayments = [33123.82, 32861.86, 32599.91, 32337.95]
        when:
            def loan = new DifferentiatedLoan(loanInfo)
        then:
            (1..loanInfo.getMonths()).each {month ->
                assert expect(
                        loan.getMonthlyPayment(month).getNumber().doubleValueExact(),
                        closeTo(monthlyPayments[month - 1], 0.01)
                )
            }
    }


    def "Тест. Получение итоговой суммы кредита"(){
        given:
            def loanInfo = new LoanInfo(
                    Money.of(124_283, "RUR"),
                    BigDecimal.valueOf(0.098),
                    LocalDate.now(),
                    27
            )
        when:
            def loan = new DifferentiatedLoan(loanInfo)
            def totalPayout = loan.getTotalPayout()
                    .getNumber()
                    .doubleValueExact()
        then:
            expect totalPayout, closeTo(138492.69, 0.01)
    }


    def "Тест. Получение остатка на месяц"(){
        given:
            def loanInfo = new LoanInfo(
                    Money.of(250_000, "RUR"),
                    BigDecimal.valueOf(0.185),
                    LocalDate.now(),
                    25
            )
        when:
            def loan = new DifferentiatedLoan(loanInfo)
        then:
            expect loan.getMonthlyCreditBalance(7).getNumber().doubleValueExact(),
                    closeTo(180_000, 0.01)
            expect loan.getMonthlyCreditBalance(22).getNumber().doubleValueExact(),
                    closeTo(30_000, 0.01)
    }


    def "Тест. Получение платежа по основному долгу в месяц"(){
        given:
        def loanInfo = new LoanInfo(
                Money.of(250_000, "RUR"),
                BigDecimal.valueOf(0.185),
                LocalDate.now(),
                25
        )
        when:
            def loan = new DifferentiatedLoan(loanInfo)
        then:
            (1..loanInfo.getMonths()).each {month ->
                assert expect(
                        loan.getMonthlyPrincipalPayout(month).getNumber().doubleValueExact(),
                        closeTo(10_000, 0.01)
                )
            }
    }


    def "Тест. Получение платежа по процентам в месяц"(){
        given:
            def loanInfo = new LoanInfo(
                    Money.of(250_000, "RUR"),
                    BigDecimal.valueOf(0.185),
                    LocalDate.now(),
                    25
            )
        when:
            def loan = new DifferentiatedLoan(loanInfo)
        then:
            expect loan.getMonthlyPercentPayout(3).getNumber().doubleValueExact(),
                    closeTo(3545.83, 0.01)
            expect loan.getMonthlyPercentPayout(17).getNumber().doubleValueExact(),
                    closeTo(1387.50, 0.01)
    }

    def "Тест. Получение всей процентной части (Переплаты)"(){
        given:
            def loanInfo = new LoanInfo(
                    Money.of(250_000, "RUR"),
                    BigDecimal.valueOf(0.185),
                    LocalDate.now(),
                    25
            )
        when:
            def loan = new DifferentiatedLoan(loanInfo)
            def totalPercentPayout = loan.getTotalPercentPayout()
                    .getNumber()
                    .doubleValueExact()
        then:
            expect totalPercentPayout, closeTo(50104.17, 0.01)
    }
}
