package com.synloans.loans.service.loan.payment

import com.synloans.loans.model.entity.loan.payment.PlannedPayment
import com.synloans.loans.repository.loan.payment.PlannedPaymentRepository
import spock.lang.Specification

class PlannedPaymentServiceTest extends Specification {
    private PlannedPaymentService paymentService
    private PlannedPaymentRepository paymentRepository

    def setup(){
        paymentRepository = Mock(PlannedPaymentRepository)
        paymentService = new PlannedPaymentService(paymentRepository)
    }

    def "Тест. Сохранение планового платежа"(){
        given:
            def payment = Stub(PlannedPayment)
        when:
            def savedPayment = paymentService.save(payment)
        then:
            savedPayment == payment
            1 * paymentRepository.save(payment) >> payment
    }

    def "Тест. Сохранение списка плановых платежей"(){
        given:
            def payments = [Stub(PlannedPayment), Stub(PlannedPayment), Stub(PlannedPayment)]
        when:
            def allPayments = paymentService.save(payments)
        then:
            allPayments == payments
            1 * paymentRepository.saveAll(payments) >> payments
    }

    def "Тест. Получение планового платежа по id"(){
        when:
            def payment = paymentService.getById(id)
        then:
            payment == resultPayment.orElse(null)
            1 * paymentRepository.findById(id) >> resultPayment
        where:
            id  || resultPayment
            2   || Optional.empty()
            10  || Optional.of(Stub(PlannedPayment))
    }
}
