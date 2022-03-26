package com.synloans.loans.mapper.converter.payments

import com.synloans.loans.model.dto.loan.payments.ActualPaymentDto
import com.synloans.loans.model.entity.loan.payment.ActualPayment
import spock.lang.Specification

import java.time.LocalDate

class ActualPaymentConverterTest extends Specification{

    private ActualPaymentConverter paymentConverter

    def setup(){
        paymentConverter = new ActualPaymentConverter()
    }


    def "Тест. Фактический платеж из entity в dto"(){
        given:
            ActualPayment actualPayment = new ActualPayment()
            actualPayment.id = 1
            actualPayment.payment = 201L
            actualPayment.date = LocalDate.now()
        when:
            ActualPaymentDto actualPaymentDto = paymentConverter.convert(actualPayment)
        then:
            verifyAll(actualPaymentDto){
                payment == actualPayment.payment
                date == actualPayment.date
            }
    }

}
