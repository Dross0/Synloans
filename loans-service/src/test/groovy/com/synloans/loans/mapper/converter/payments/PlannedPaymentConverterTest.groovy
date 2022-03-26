package com.synloans.loans.mapper.converter.payments

import com.synloans.loans.model.dto.loan.payments.PlannedPaymentDto
import com.synloans.loans.model.entity.loan.payment.PlannedPayment
import spock.lang.Specification

import java.time.LocalDate

class PlannedPaymentConverterTest extends Specification{

    private PlannedPaymentConverter paymentConverter

    def setup(){
        paymentConverter = new PlannedPaymentConverter()
    }

    def "Тест. Планновый платеж из entity в dto"(){
        given:
            PlannedPayment payment = new PlannedPayment()
            payment.id = 1
            payment.percent = 100L
            payment.principal = 212L
            payment.date = LocalDate.now()
        when:
            PlannedPaymentDto plannedPaymentDto = paymentConverter.convert(payment)
        then:
            verifyAll(plannedPaymentDto){
                principal == payment.principal
                percent == payment.percent
                date == payment.date
            }
    }
}
