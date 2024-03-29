package com.synloans.loans.service.loan

import com.synloans.loans.mapper.converter.CompanyNodeConverter
import com.synloans.loans.model.dto.loan.payments.PaymentRequest
import com.synloans.loans.model.dto.loanrequest.LoanRequestStatus
import com.synloans.loans.model.entity.company.Bank
import com.synloans.loans.model.entity.company.Company
import com.synloans.loans.model.entity.loan.Loan
import com.synloans.loans.model.entity.loan.LoanRequest
import com.synloans.loans.model.entity.loan.payment.ActualPayment
import com.synloans.loans.model.entity.loan.payment.PlannedPayment
import com.synloans.loans.model.entity.syndicate.Syndicate
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant
import com.synloans.loans.model.entity.user.User
import com.synloans.loans.repository.loan.LoanRepository
import com.synloans.loans.service.blockchain.BlockchainService
import com.synloans.loans.service.exception.AcceptPaymentException
import com.synloans.loans.service.exception.ForbiddenResourceException
import com.synloans.loans.service.exception.InvalidLoanRequestException
import com.synloans.loans.service.exception.notfound.LoanNotFoundException
import com.synloans.loans.service.exception.notfound.LoanRequestNotFoundException
import com.synloans.loans.service.loan.payment.actual.ActualPaymentService
import com.synloans.loans.service.loan.payment.planned.PlannedPaymentService
import com.synloans.loans.service.syndicate.participant.SyndicateParticipantService
import com.synloans.loans.service.syndicate.participant.impl.SyndicateParticipantServiceImpl
import spock.lang.Specification

import java.time.LocalDate

class LoanServiceTest extends Specification{
    private LoanService loanService
    private LoanRepository loanRepository
    private LoanRequestService loanRequestService
    private SyndicateParticipantService participantService
    private PlannedPaymentService plannedPaymentService
    private ActualPaymentService actualPaymentService
    private BlockchainService blockchainService
    private CompanyNodeConverter companyNodeConverter

    def setup(){
        loanRepository = Mock(LoanRepository)
        loanRequestService = Mock(LoanRequestService)
        participantService = Mock(SyndicateParticipantServiceImpl)
        plannedPaymentService = Mock(PlannedPaymentService)
        actualPaymentService = Mock(ActualPaymentService)
        blockchainService = Mock(BlockchainService)
        companyNodeConverter = Mock(CompanyNodeConverter)

        loanService = new LoanService(
                loanRepository,
                loanRequestService,
                participantService,
                plannedPaymentService,
                actualPaymentService,
                blockchainService,
                companyNodeConverter
        )
    }

    def "Тест. Ошибка при старте кредита по заявке. Кредит уже существует"(){
        given:
            def loanRq = Stub(LoanRequest){
                loan  >> Stub(Loan)
            }
        when:
            loanService.startLoan(loanRq)
        then:
            thrown(InvalidLoanRequestException)
    }

    def "Тест. Ошибка при старте кредита по заявке. Кредит не готов к выдаче"(){
        given:
            def loanRq = Stub(LoanRequest){
                loan >> null
                sum >> 1_000_000
            }
        when:
            loanService.startLoan(loanRq)
        then:
            1 * loanRequestService.getStatus(loanRq) >> status
            thrown(InvalidLoanRequestException)

        where:
            status << [LoanRequestStatus.OPEN, LoanRequestStatus.CLOSE, LoanRequestStatus.ISSUE]
    }

    def "Тест. Старт кредита по заявке"(){
        given:

            def participant1 = new SyndicateParticipant()
            participant1.loanSum = 500_000
            participant1.approveBankAgent = false
            participant1.bank = Stub(Bank)

            def participant2 = new SyndicateParticipant()
            participant2.loanSum = 300_000
            participant2.approveBankAgent = true
            participant2.bank = Stub(Bank)


            def participant3 = new SyndicateParticipant()
            participant3.loanSum = 250_000
            participant3.approveBankAgent = true
            participant3.bank = Stub(Bank)


            def participant4 = new SyndicateParticipant()
            participant4.loanSum = 100_000
            participant4.approveBankAgent = true
            participant4.bank = Stub(Bank)


            def syndicate = Stub(Syndicate){
                participants >> [participant4, participant2, participant3, participant1]
            }

            def loanRq = Stub(LoanRequest){
                loan >> null
                sum >> 1_000_000
                rate >> 10.2
                term >> 12
                it.syndicate >> syndicate
            }
        when:
            def loan = loanService.startLoan(loanRq)
        then:
            1 * loanRequestService.getStatus(loanRq) >> LoanRequestStatus.READY_TO_ISSUE
            1 * participantService.saveAll(_)
            1 * loanRepository.save(_ as Loan) >> {Loan l -> l}
            1 * plannedPaymentService.save(_)
            loan.rate == loanRq.rate
            loan.registrationDate == LocalDate.now()
            loan.closeDate == LocalDate.now().plusMonths(loanRq.term)
            loan.sum == loanRq.sum
            loan.bankAgent == participant2.bank
            loan.request == loanRq

            participant1.issuedLoanSum == 500_000
            participant2.issuedLoanSum == 300_000
            participant3.issuedLoanSum == 200_000
            participant4.issuedLoanSum == null
    }

    def "Тест. Старт кредита по id заявки"(){
        given:

            def participant1 = new SyndicateParticipant()
            participant1.loanSum = 500_000
            participant1.approveBankAgent = false
            participant1.bank = Stub(Bank)

            def participant2 = new SyndicateParticipant()
            participant2.loanSum = 300_000
            participant2.approveBankAgent = true
            participant2.bank = Stub(Bank)


            def participant3 = new SyndicateParticipant()
            participant3.loanSum = 250_000
            participant3.approveBankAgent = true
            participant3.bank = Stub(Bank)


            def participant4 = new SyndicateParticipant()
            participant4.loanSum = 100_000
            participant4.approveBankAgent = true
            participant4.bank = Stub(Bank)


            def syndicate = Stub(Syndicate){
                participants >> [participant4, participant2, participant3, participant1]
            }

            Company company = Stub(Company){
                id >> 133
            }

            def user = Stub(User){
                it.company >> company
            }

            def loanRq = Stub(LoanRequest){
                id >> 10
                loan >> null
                sum >> 1_000_000
                rate >> 10.2
                term >> 12
                it.syndicate >> syndicate
                it.company >> company
            }
        when:
            def loan = loanService.startLoanByRequestId(loanRq.id, user)
        then:
            1 * loanRequestService.getById(loanRq.id) >> Optional.of(loanRq)
            1 * loanRequestService.getStatus(loanRq) >> LoanRequestStatus.READY_TO_ISSUE
            1 * participantService.saveAll(_)
            1 * loanRepository.save(_ as Loan) >> {Loan l -> l}
            1 * plannedPaymentService.save(_)
            loan.rate == loanRq.rate
            loan.registrationDate == LocalDate.now()
            loan.closeDate == LocalDate.now().plusMonths(loanRq.term)
            loan.sum == loanRq.sum
            loan.bankAgent == participant2.bank
            loan.request == loanRq

            participant1.issuedLoanSum == 500_000
            participant2.issuedLoanSum == 300_000
            participant3.issuedLoanSum == 200_000
            participant4.issuedLoanSum == null
    }

    def "Тест. Ошибка при старте кредита по id несуществующей заявки"(){
        given:
            def requestId = 10

        when:
            loanService.startLoanByRequestId(requestId, new User())

        then:
            1 * loanRequestService.getById(requestId) >> Optional.empty()
            thrown(LoanRequestNotFoundException)
    }

    def "Тест. Ошибка при старте кредита по id заявки от другого пользователя"(){
        given:
            def requestId = 10

            Company userCompany = new Company()
            userCompany.id = 12

            User user = new User()
            user.company = userCompany

            Company requestCompany = new Company()
            requestCompany.id = 3131

            LoanRequest loanRequest = new LoanRequest()
            loanRequest.company = requestCompany

        when:
            loanService.startLoanByRequestId(requestId, user)

        then:
            1 * loanRequestService.getById(requestId) >> Optional.of(loanRequest)
            thrown(ForbiddenResourceException)
    }

    def "Тест. Сохранение кредита"(){
        given:
            def loan = Stub(Loan)
        when:
            def savedLoan = loanService.save(loan)
        then:
            1 * loanRepository.save(loan) >> loan
            savedLoan == loan
    }

    def "Тест. Получение кредита по id заявки"(){
        given:
            def requestId = 10
            Loan expectedLoan = new Loan()
            LoanRequest loanRequest = new LoanRequest()
            loanRequest.loan = expectedLoan
        when:
            def loan = loanService.getLoanByRequestId(requestId)
        then:
            1 * loanRequestService.getById(requestId) >> Optional.of(loanRequest)
            loan == expectedLoan
    }

    def "Тест. Ошибка при получении кредита по id несуществующей заявки"(){
        given:
            def requestId = 10

        when:
            loanService.getLoanByRequestId(requestId)

        then:
            1 * loanRequestService.getById(requestId) >> Optional.empty()
            thrown(LoanRequestNotFoundException)
    }

    def "Тест. Ошибка при получении кредита по id заявки с не созданным кредитом"(){
        given:
            def requestId = 10
            LoanRequest loanRequest = new LoanRequest()
            loanRequest.loan == null

        when:
            loanService.getLoanByRequestId(requestId)

        then:
            1 * loanRequestService.getById(requestId) >> Optional.of(loanRequest)
            thrown(LoanNotFoundException)
    }

    def "Тест. Получение планновых платежей кредита по id заявки"(){
        given:
            def requestId = 10

            List<PlannedPayment> expectedPayments = [new PlannedPayment(), new PlannedPayment()]

            Company company = new Company()
            company.id = 12

            Syndicate syndicate = new Syndicate()
            syndicate.participants = []

            Loan loan = new Loan()
            loan.plannedPayments = expectedPayments

            LoanRequest loanRequest = new LoanRequest()
            loanRequest.loan = loan
            loanRequest.company = company
            loanRequest.syndicate = syndicate

            loan.request = loanRequest
        when:
            def payments = loanService.getPlannedPaymentsByRequestId(requestId, company)
        then:
            1 * loanRequestService.getById(requestId) >> Optional.of(loanRequest)
            payments == expectedPayments
    }

    def "Тест. Получение планновых платежей кредита по id заявки не от участника кредита"(){
        given:
            def requestId = 10

            List<PlannedPayment> expectedPayments = [new PlannedPayment(), new PlannedPayment()]

            Company company = new Company()
            company.id = 12

            Company participantCompany = new Company()
            participantCompany.id = 99
            Bank bank = new Bank()
            bank.company = participantCompany
            SyndicateParticipant syndicateParticipant = new SyndicateParticipant()
            syndicateParticipant.bank = bank
            syndicateParticipant.issuedLoanSum = 1000L

            Syndicate syndicate = new Syndicate()
            syndicate.participants = [syndicateParticipant] as Set

            Loan loan = new Loan()
            loan.plannedPayments = expectedPayments

            Company borrower = new Company()
            borrower.id = 134

            LoanRequest loanRequest = new LoanRequest()
            loanRequest.loan = loan
            loanRequest.company = borrower
            loanRequest.syndicate = syndicate

            loan.request = loanRequest
        when:
            loanService.getPlannedPaymentsByRequestId(requestId, company)
        then:
            1 * loanRequestService.getById(requestId) >> Optional.of(loanRequest)
            thrown(ForbiddenResourceException)
    }



    def "Тест. Ошибка при получении планновых платежей кредита по id несуществующей заявки"(){
        given:
            def requestId = 10

        when:
            loanService.getPlannedPaymentsByRequestId(requestId, new Company())

        then:
            1 * loanRequestService.getById(requestId) >> Optional.empty()
            thrown(LoanRequestNotFoundException)
    }

    def "Тест. Ошибка при получении планновых платежей кредита по id заявки с не созданным кредитом"(){
        given:
            def requestId = 10
            LoanRequest loanRequest = new LoanRequest()
            loanRequest.loan == null

        when:
            loanService.getPlannedPaymentsByRequestId(requestId, new Company())

        then:
            1 * loanRequestService.getById(requestId) >> Optional.of(loanRequest)
            thrown(LoanNotFoundException)
    }

    def "Тест. Получение фактических платежей кредита по id заявки"(){
        given:
            def requestId = 10

            List<ActualPayment> expectedPayments = [new ActualPayment(), new ActualPayment()]

            Company company = new Company()
            company.id = 12

            Syndicate syndicate = new Syndicate()
            syndicate.participants = []

            Loan loan = new Loan()
            loan.actualPayments = expectedPayments

            LoanRequest loanRequest = new LoanRequest()
            loanRequest.loan = loan
            loanRequest.company = company
            loanRequest.syndicate = syndicate

            loan.request = loanRequest
        when:
            def payments = loanService.getActualPaymentsByRequestId(requestId, company)
        then:
            1 * loanRequestService.getById(requestId) >> Optional.of(loanRequest)
            payments == expectedPayments
    }

    def "Тест. Получение фактических платежей кредита по id заявки не от участника кредита"(){
        given:
            def requestId = 10

            List<ActualPayment> expectedPayments = [new ActualPayment(), new ActualPayment()]

            Company company = new Company()
            company.id = 12

            Company participantCompany = new Company()
            participantCompany.id = 99
            Bank bank = new Bank()
            bank.company = participantCompany
            SyndicateParticipant syndicateParticipant = new SyndicateParticipant()
            syndicateParticipant.bank = bank
            syndicateParticipant.issuedLoanSum = 1000L

            Syndicate syndicate = new Syndicate()
            syndicate.participants = [syndicateParticipant] as Set

            Loan loan = new Loan()
            loan.actualPayments = expectedPayments

            Company borrower = new Company()
            borrower.id = 134

            LoanRequest loanRequest = new LoanRequest()
            loanRequest.loan = loan
            loanRequest.company = borrower
            loanRequest.syndicate = syndicate

            loan.request = loanRequest
        when:
            loanService.getActualPaymentsByRequestId(requestId, company)
        then:
            1 * loanRequestService.getById(requestId) >> Optional.of(loanRequest)
            thrown(ForbiddenResourceException)
    }

    def "Тест. Ошибка при получении фактических платежей кредита по id несуществующей заявки"(){
        given:
            def requestId = 10

        when:
            loanService.getActualPaymentsByRequestId(requestId, new Company())

        then:
            1 * loanRequestService.getById(requestId) >> Optional.empty()
            thrown(LoanRequestNotFoundException)
    }

    def "Тест. Ошибка при получении фактических платежей кредита по id заявки с не созданным кредитом"(){
        given:
            def requestId = 10
            LoanRequest loanRequest = new LoanRequest()
            loanRequest.loan == null

        when:
            loanService.getActualPaymentsByRequestId(requestId, new Company())

        then:
            1 * loanRequestService.getById(requestId) >> Optional.of(loanRequest)
            thrown(LoanNotFoundException)
    }

    def "Тест. Проведение платежа"(){
        given:
        def requestId = 123

        Company company = new Company()
        company.id = 10

        User user = new User()
        user.company = company

        Loan loan = new Loan()

        LoanRequest loanRequest = new LoanRequest()
        loanRequest.loan = loan
        loanRequest.company = company
        loan.request = loanRequest

        PaymentRequest paymentRequest = new PaymentRequest()

        ActualPayment expectedActualPayment = new ActualPayment()
        when:
            ActualPayment actualPayment = loanService.acceptPayment(requestId, paymentRequest, user)

        then:
            1 * loanRequestService.getById(requestId) >> Optional.of(loanRequest)
            1 * actualPaymentService.createPayment(loan, paymentRequest) >> expectedActualPayment
            actualPayment == expectedActualPayment
    }

    def "Тест. Ошибка проведении платежа по id несуществующей заявки"(){
        given:
            def requestId = 10

        when:
            loanService.acceptPayment(requestId, new PaymentRequest(), new User())

        then:
            1 * loanRequestService.getById(requestId) >> Optional.empty()
            thrown(LoanRequestNotFoundException)
    }

    def "Тест. Ошибка проведении платежа по id заявки с не созданным кредитом"(){
        given:
            def requestId = 10
            LoanRequest loanRequest = new LoanRequest()
            loanRequest.loan == null

        when:
            loanService.acceptPayment(requestId, new PaymentRequest(), new User())

        then:
            1 * loanRequestService.getById(requestId) >> Optional.of(loanRequest)
            thrown(LoanNotFoundException)
    }

    def "Тест. Ошибка при проведении платежа по id заявки от другого пользователя"(){
        given:
            def requestId = 10

            Company userCompany = new Company()
            userCompany.id = 12

            User user = new User()
            user.company = userCompany

            Company requestCompany = new Company()
            requestCompany.id = 3131

            Loan loan = new Loan()

            LoanRequest loanRequest = new LoanRequest()
            loanRequest.loan = loan
            loanRequest.company = requestCompany
            loan.request = loanRequest

        when:
            loanService.acceptPayment(requestId, new PaymentRequest(), user)

        then:
            1 * loanRequestService.getById(requestId) >> Optional.of(loanRequest)
            thrown(ForbiddenResourceException)
    }

    def "Тест. Ошибка в сервисе платежей при проведении платежа"(){
        given:
            def requestId = 123

            Company company = new Company()
            company.id = 10

            User user = new User()
            user.company = company

            Loan loan = new Loan()

            LoanRequest loanRequest = new LoanRequest()
            loanRequest.loan = loan
            loanRequest.company = company
            loan.request = loanRequest

            PaymentRequest paymentRequest = new PaymentRequest()

        when:
            loanService.acceptPayment(requestId, paymentRequest, user)

        then:
            1 * loanRequestService.getById(requestId) >> Optional.of(loanRequest)
            1 * actualPaymentService.createPayment(loan, paymentRequest) >> {throw new IllegalArgumentException()}
            thrown(AcceptPaymentException)
    }

    //FIXME add test to blockchain persist
}
