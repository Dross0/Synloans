package com.synloans.loans.service.syndicate

import com.synloans.loans.model.dto.SyndicateJoinRequest
import com.synloans.loans.model.entity.company.Bank
import com.synloans.loans.model.entity.loan.LoanRequest
import com.synloans.loans.model.entity.syndicate.Syndicate
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant
import com.synloans.loans.repository.syndicate.SyndicateRepository
import com.synloans.loans.service.exception.notfound.LoanRequestNotFoundException
import com.synloans.loans.service.loan.LoanRequestService
import spock.lang.Specification

class SyndicateServiceTest extends Specification {
    private SyndicateService syndicateService
    private SyndicateRepository syndicateRepository
    private LoanRequestService loanRequestService
    private SyndicateParticipantService syndicateParticipantService

    def setup(){
        syndicateRepository = Mock(SyndicateRepository)
        loanRequestService = Mock(LoanRequestService)
        syndicateParticipantService = Mock(SyndicateParticipantService)
        syndicateService = new SyndicateService(syndicateRepository, loanRequestService, syndicateParticipantService)
    }


    def "Тест. Получение синдиката по id заявки"(){
        when:
            def createdSyndicate = syndicateService.getByLoanRequestId(loanRequestId)
        then:
            1 * syndicateRepository.findByRequest_Id(loanRequestId) >> syndicate
            0 * loanRequestService.getById(_)
            0 * syndicateRepository.save(_)

            createdSyndicate == syndicate
        where:
            loanRequestId || syndicate
            101           || null
            190           || Stub(Syndicate)
    }

    def "Тест. Вступление банка в существующий синдикат"(){
        given:
            def joinRq = Stub(SyndicateJoinRequest){
                requestId >> 20
                sum >> 100_000
                approveBankAgent >> true
            }
            def syndicate = Stub(Syndicate)
            def bank = Stub(Bank)
            def participant = Stub(SyndicateParticipant)
        when:
            def participantOp = syndicateService.joinBankToSyndicate(joinRq, bank)
        then:
            1 * syndicateRepository.findByRequest_Id(joinRq.requestId) >> syndicate
            0 * syndicateRepository.save(_)
            1 * syndicateParticipantService.createNewParticipant(
                    syndicate,
                    bank,
                    100_000,
                    joinRq.approveBankAgent
            ) >> participant

            participant == participantOp.orElse(null)
            noExceptionThrown()
    }

    def "Тест. Вступление банка в новый синдикат"(){
        given:
            def joinRq = Stub(SyndicateJoinRequest){
                requestId >> 20
                sum >> 100_000
                approveBankAgent >> true
            }
            def bank = Stub(Bank)
            def participant = Stub(SyndicateParticipant)
        when:
            def participantOp = syndicateService.joinBankToSyndicate(joinRq, bank)
        then:
            1 * syndicateRepository.findByRequest_Id(joinRq.requestId) >> null
            1 * loanRequestService.getById(joinRq.requestId) >> Optional.of(Stub(LoanRequest))
            1 * syndicateRepository.save(_ as Syndicate) >> {Syndicate s -> s}
            1 * syndicateParticipantService.createNewParticipant(
                    _ as Syndicate,
                    bank,
                    100_000,
                    joinRq.approveBankAgent
            ) >> participant

            participant == participantOp.orElse(null)
            noExceptionThrown()
    }

    def "Тест. Вступление банка в новый синдикат с несуществующей заявкой"(){
        given:
            def joinRq = Stub(SyndicateJoinRequest){
                requestId >> 20
                sum >> 100_000
                approveBankAgent >> true
            }
            def bank = Stub(Bank)
        when:
            syndicateService.joinBankToSyndicate(joinRq, bank)
        then:
            1 * syndicateRepository.findByRequest_Id(joinRq.requestId) >> null
            1 * loanRequestService.getById(joinRq.requestId) >> Optional.empty()
            0 * syndicateRepository.save(_ as Syndicate)
            0 * syndicateParticipantService.createNewParticipant(_, _ , _, _)
            thrown(LoanRequestNotFoundException)
    }

}
