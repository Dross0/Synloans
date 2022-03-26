package com.synloans.loans.service.loan.payment

import com.synloans.loans.model.dto.loan.payments.PaymentRequest
import com.synloans.loans.model.entity.loan.Loan
import com.synloans.loans.model.entity.loan.payment.ActualPayment
import com.synloans.loans.repository.loan.payment.ActualPaymentRepository
import spock.lang.Specification

import java.time.LocalDate

class ActualPaymentServiceTest extends Specification {
    private ActualPaymentService paymentService
    private ActualPaymentRepository paymentRepository

    def setup(){
        paymentRepository = Mock(ActualPaymentRepository)
        paymentService = new ActualPaymentService(paymentRepository)
    }

    def "Тест. Сохранение фактического платежа"(){
        given:
            def payment = Stub(ActualPayment)
        when:
            def savedPayment = paymentService.save(payment)
        then:
            savedPayment == payment
            1 * paymentRepository.save(payment) >> payment
    }

    def "Тест. Сохранение списка фактических платежей"(){
        given:
            def payments = [Stub(ActualPayment), Stub(ActualPayment), Stub(ActualPayment)]
        when:
            def allPayments = paymentService.save(payments)
        then:
            allPayments == payments
            1 * paymentRepository.saveAll(payments) >> payments
    }

    def "Тест. Получение фактического платежа по id"(){
        when:
            def payment = paymentService.getById(id)
        then:
            payment == resultPayment.orElse(null)
            1 * paymentRepository.findById(id) >> resultPayment
        where:
            id  || resultPayment
            2   || Optional.empty()
            10  || Optional.of(Stub(ActualPayment))
    }

    def "Тест. Создание фактического платежа"(){
        given:
        Loan loan = new Loan()
        loan.actualPayments = []

        PaymentRequest paymentRequest = new PaymentRequest()
        paymentRequest.payment = 1000l
        paymentRequest.date = LocalDate.now()

        when:
            ActualPayment actualPayment = paymentService.createPayment(loan, paymentRequest)

        then:
            1 * paymentRepository.save(_) >> {ActualPayment payment ->
                return payment
            }
            assert loan.actualPayments.contains(actualPayment)
            assert actualPayment.payment == paymentRequest.payment
            assert actualPayment.date == paymentRequest.date
            assert actualPayment.loan == loan
    }
}
