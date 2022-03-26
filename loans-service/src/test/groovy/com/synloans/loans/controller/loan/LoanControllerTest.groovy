package com.synloans.loans.controller.loan

import com.synloans.loans.mapper.converter.payments.ActualPaymentConverter
import com.synloans.loans.mapper.converter.payments.PlannedPaymentConverter
import com.synloans.loans.model.dto.loan.LoanCreateResponse
import com.synloans.loans.model.dto.loan.payments.ActualPaymentDto
import com.synloans.loans.model.dto.loan.payments.PaymentRequest
import com.synloans.loans.model.dto.loan.payments.PlannedPaymentDto
import com.synloans.loans.model.entity.loan.Loan
import com.synloans.loans.model.entity.loan.payment.ActualPayment
import com.synloans.loans.model.entity.loan.payment.PlannedPayment
import com.synloans.loans.model.entity.user.User
import com.synloans.loans.service.loan.LoanService
import com.synloans.loans.service.user.UserService
import org.springframework.security.core.Authentication
import spock.lang.Specification

class LoanControllerTest extends Specification{

    private LoanController loanController

    private LoanService loanService

    private UserService userService

    private PlannedPaymentConverter plannedPaymentConverter

    private ActualPaymentConverter actualPaymentConverter

    def setup(){
        loanService = Mock(LoanService)
        userService = Mock(UserService)
        plannedPaymentConverter = Mock(PlannedPaymentConverter)
        actualPaymentConverter = Mock(ActualPaymentConverter)

        loanController = new LoanController(
                loanService,
                userService,
                plannedPaymentConverter,
                actualPaymentConverter
        )
    }

    def "Тест. Старт кредита по id заявки"(){
        given:
            def requestId = 10

            Authentication authentication = Mock(Authentication)
            User user = new User()
            Loan loan = new Loan()
            loan.id = 311

        when:
            LoanCreateResponse response = loanController.startLoan(requestId, authentication)

        then:
            1 * userService.getCurrentUser(authentication) >> user
            1 * loanService.startLoanByRequestId(requestId, user) >> loan

            response.loanId == loan.id
            response.loanRequestId == requestId
    }

    def "Тест. Получение списка плановых платежей"(){
        given:
            def requestId = 10

            PlannedPayment payment1 = new PlannedPayment()
            PlannedPayment payment2 = new PlannedPayment()

            PlannedPaymentDto dto1 = Mock(PlannedPaymentDto)
            PlannedPaymentDto dto2 = Mock(PlannedPaymentDto)

        when:
            List<PlannedPaymentDto> paymentDtoList = loanController.getPlannedPayments(requestId)

        then:
            1 * loanService.getPlannedPaymentsByRequestId(requestId) >> [payment1, payment2]
            1 * plannedPaymentConverter.convert(payment1) >> dto1
            1 * plannedPaymentConverter.convert(payment2) >> dto2
            paymentDtoList == [dto1, dto2]
    }

    def "Тест. Получение списка фактических платежей"(){
        given:
            def requestId = 10

            ActualPayment payment1 = new ActualPayment()
            ActualPayment payment2 = new ActualPayment()

            ActualPaymentDto dto1 = Mock(ActualPaymentDto)
            ActualPaymentDto dto2 = Mock(ActualPaymentDto)

        when:
            List<ActualPaymentDto> paymentDtoList = loanController.getActualPayments(requestId)

        then:
            1 * loanService.getActualPaymentsByRequestId(requestId) >> [payment1, payment2]
            1 * actualPaymentConverter.convert(payment1) >> dto1
            1 * actualPaymentConverter.convert(payment2) >> dto2
            paymentDtoList == [dto1, dto2]
    }

    def "Тест. Проведение платежа"(){
        given:
            def requestId = 10

            ActualPayment createdPayment = new ActualPayment()

            PaymentRequest paymentRequest = new PaymentRequest()

            Authentication authentication = Mock(Authentication)

            User user = new User()

            ActualPaymentDto expectedPaymentDto = Mock(ActualPaymentDto)

        when:
            ActualPaymentDto paymentDto = loanController.acceptPayment(requestId, paymentRequest, authentication)

        then:
            1 * userService.getCurrentUser(authentication) >> user
            1 * loanService.acceptPayment(requestId, paymentRequest, user) >> createdPayment
            1 * actualPaymentConverter.convert(createdPayment) >> expectedPaymentDto
            paymentDto == expectedPaymentDto
    }
}
