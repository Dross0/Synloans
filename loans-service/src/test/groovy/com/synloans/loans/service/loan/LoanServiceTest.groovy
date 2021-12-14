package com.synloans.loans.service.loan


import com.synloans.loans.model.entity.company.Bank
import com.synloans.loans.model.entity.loan.Loan
import com.synloans.loans.model.entity.loan.LoanRequest
import com.synloans.loans.model.entity.syndicate.Syndicate
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant
import com.synloans.loans.repository.loan.LoanRepository
import com.synloans.loans.service.exception.InvalidLoanRequestException
import com.synloans.loans.service.syndicate.SyndicateParticipantService
import spock.lang.Specification

import java.time.LocalDate

class LoanServiceTest extends Specification{
    private LoanService loanService
    private LoanRepository loanRepository
    private LoanRequestService loanRequestService
    private SyndicateParticipantService participantService

    def setup(){
        loanRepository = Mock(LoanRepository)
        loanRequestService = Mock(LoanRequestService)
        participantService = Mock(SyndicateParticipantService)
        loanService = new LoanService(loanRepository, loanRequestService, participantService)
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

    def "Тест. Ошибка при старте кредита по заявке. Суммы набранной в синдикате недостаточно"(){
        given:
            def loanRq = Stub(LoanRequest){
                loan >> null
                sum >> 1_000_000
            }
        when:
            def loan = loanService.startLoan(loanRq)
        then:
            1 * loanRequestService.calcSumFromSyndicate(loanRq) >> 880_000
            thrown(InvalidLoanRequestException)
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
            1 * loanRequestService.calcSumFromSyndicate(loanRq) >> 1_150_000
            1 * participantService.saveAll(_)
            1 * loanRepository.save(_ as Loan) >> {Loan l -> l}
            loan.rate == loanRq.rate
            loan.registrationDate == LocalDate.now()
            loan.closeDate == LocalDate.now().plusMonths(loanRq.term)
            loan.sum == loanRq.sum as double
            loan.bankAgent == participant2.bank
            loan.request == loanRq

            participant1.issuedLoanSum == 500_000
            participant2.issuedLoanSum == 300_000
            participant3.issuedLoanSum == 200_000
            participant4.issuedLoanSum == null
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
}
