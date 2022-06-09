package com.synloans.loans.factory

import com.synloans.loans.AnnuityLoan
import com.synloans.loans.model.info.LoanInfo
import org.javamoney.moneta.Money
import spock.lang.Specification

import java.time.LocalDate

class AnnuityLoanFactoryTest extends Specification{
    def "Тест. Создание кредита через фабрику"(){
        given:
            def loanInfo = new LoanInfo(
                    Money.of(1000, "RUR"),
                    BigDecimal.valueOf(0.1),
                    LocalDate.now(),
                    12
            )
        when:
            def loan = new AnnuityLoanFactory().create(loanInfo)
        then:
            loan instanceof AnnuityLoan
            loan.getInfo() == loanInfo
            noExceptionThrown()
    }
}
