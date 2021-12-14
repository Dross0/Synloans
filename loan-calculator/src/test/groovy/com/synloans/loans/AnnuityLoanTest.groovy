package com.synloans.loans

import com.synloans.loans.info.LoanInfo
import org.javamoney.moneta.Money
import spock.lang.Specification

import java.time.LocalDate

import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.expect

class AnnuityLoanTest extends Specification{
    def "Тест. Создание кредита"() {
        given:
            def loanInfo = new LoanInfo(
                    Money.of(1000, "RUR"),
                    BigDecimal.valueOf(0.1),
                    LocalDate.now(),
                    12
            )
        when:
            def annuityLoan = new AnnuityLoan(loanInfo)
        then:
            annuityLoan.getInfo() == loanInfo
            noExceptionThrown()
    }

    def "Тест. Получение платежа в месяц"(){
        given:
            def loanInfo = new LoanInfo(
                    Money.of(12_478, "RUR"),
                    BigDecimal.valueOf(0.098),
                    LocalDate.now(),
                    11
            )
        when:
            def annuityLoan = new AnnuityLoan(loanInfo)
        then:
        (1..loanInfo.getMonths()).each {month ->
            assert expect(
                    annuityLoan.getMonthlyPayment(month).getNumber().doubleValueExact(),
                    closeTo(1190.7, 0.001)
            )
        }
    }

    def "Тест. Получение итоговой суммы кредита"(){
        given:
            def loanInfo = new LoanInfo(
                    Money.of(98_763, "RUR"),
                    BigDecimal.valueOf(0.135),
                    LocalDate.now(),
                    23
            )
        when:
            def annuityLoan = new AnnuityLoan(loanInfo)
            def totalPayout = annuityLoan.getTotalPayout()
                    .getNumber()
                    .doubleValueExact()
        then:
            expect totalPayout, closeTo(112642.31, 0.01)
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
            def annuityLoan = new AnnuityLoan(loanInfo)
        then:
            expect annuityLoan.getMonthlyCreditBalance(16).getNumber().doubleValueExact(),
                    closeTo(101180.28, 0.01)
            expect annuityLoan.getMonthlyCreditBalance(20).getNumber().doubleValueExact(),
                    closeTo(57926.17, 0.01)
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
            def annuityLoan = new AnnuityLoan(loanInfo)
        then:
            expect annuityLoan.getMonthlyPrincipalPayout(4).getNumber().doubleValueExact(),
                    closeTo(8660.87, 0.01)
            expect annuityLoan.getMonthlyPrincipalPayout(23).getNumber().doubleValueExact(),
                    closeTo(11582.52, 0.01)
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
            def annuityLoan = new AnnuityLoan(loanInfo)
        then:
            expect annuityLoan.getMonthlyPercentPayout(2).getNumber().doubleValueExact(),
                    closeTo(3726.63, 0.01)
            expect annuityLoan.getMonthlyPercentPayout(21).getNumber().doubleValueExact(),
                    closeTo(893.03, 0.01)
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
            def annuityLoan = new AnnuityLoan(loanInfo)
            def totalPercentPayout = annuityLoan.getTotalPercentPayout()
                    .getNumber()
                    .doubleValueExact()
        then:
            expect totalPercentPayout, closeTo(53162.89, 0.01)

    }
}
