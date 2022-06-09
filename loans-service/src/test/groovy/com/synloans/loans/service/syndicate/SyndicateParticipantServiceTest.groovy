package com.synloans.loans.service.syndicate

import com.synloans.loans.model.entity.company.Bank
import com.synloans.loans.model.entity.loan.Loan
import com.synloans.loans.model.entity.loan.LoanRequest
import com.synloans.loans.model.entity.syndicate.Syndicate
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant
import com.synloans.loans.repository.syndicate.SyndicateParticipantRepository
import com.synloans.loans.service.exception.SyndicateQuitException
import com.synloans.loans.service.exception.notfound.LoanRequestNotFoundException
import com.synloans.loans.service.loan.LoanRequestService
import spock.lang.Specification

class SyndicateParticipantServiceTest extends Specification{
    private SyndicateParticipantService participantService
    private SyndicateParticipantRepository participantRepository
    private LoanRequestService loanRequestService

    def setup(){
        participantRepository = Mock(SyndicateParticipantRepository)
        loanRequestService = Mock(LoanRequestService)
        participantService = new SyndicateParticipantService(participantRepository, loanRequestService)
    }

    def "Тест. Создание участника синдиката"(){
        given:
            def syndicate = Stub(Syndicate)
            def bank = Stub(Bank)
            def loanSum = 123_003_000
            def approveBankAgent = true
        when:
            def participant = participantService.createNewParticipant(syndicate, bank, loanSum, approveBankAgent)
        then:
            1 * participantRepository.save(_ as SyndicateParticipant) >> {SyndicateParticipant p -> p}
            participant.syndicate == syndicate
            participant.bank == bank
            participant.loanSum == loanSum
            participant.approveBankAgent == approveBankAgent
            participant.issuedLoanSum == null
    }

    def "Тест. Выход участника из синдиката"(){
        given:
            def idToDelete = 103
            def participantToDelete = createParticipant(idToDelete)
            Set<SyndicateParticipant> participants = [
                    createParticipant(1),
                    participantToDelete,
                    createParticipant(3),
                    createParticipant(111)
            ] as Set
            def bank = Stub(Bank)
            bank.syndicates >> participants
        when:
            participantService.quitFromSyndicate(idToDelete, bank)
        then:
            1 * participantRepository.delete(participantToDelete)
    }

    def "Тест. Выход участника из синдиката, когда кредит выдан"(){
        given:
            def idToDelete = 103
            def participantToDelete = createParticipant(idToDelete)
            participantToDelete.syndicate.request.loan = new Loan()
            Set<SyndicateParticipant> participants = [
                    createParticipant(1),
                    participantToDelete,
                    createParticipant(3),
                    createParticipant(111)
            ] as Set
            def bank = Stub(Bank)
            bank.syndicates >> participants
        when:
            participantService.quitFromSyndicate(idToDelete, bank)
        then:
            0 * participantRepository.delete(_)
            thrown(SyndicateQuitException)
    }

    def "Тест. Участники синдиката по id заявки"(){
        given:
            def loanRqId = 11
            def participants = [Stub(SyndicateParticipant), Stub(SyndicateParticipant), Stub(SyndicateParticipant)] as Set
            def loanRq = Stub(LoanRequest){
                id >> loanRqId
                syndicate >> Stub(Syndicate){
                    it.participants >> participants
                }
            }
        when:
            def result = participantService.getSyndicateParticipantsByRequestId(loanRqId)
        then:
            1 * loanRequestService.getById(loanRqId) >> Optional.of(loanRq)
            result == participants
    }

    def "Тест. Участники синдиката по id заявки когда нет синдиката"(){
        given:
            def loanRqId = 11
            def loanRq = Stub(LoanRequest){
                id >> loanRqId
                syndicate >> null
            }
        when:
            def result = participantService.getSyndicateParticipantsByRequestId(loanRqId)
        then:
            1 * loanRequestService.getById(loanRqId) >> Optional.of(loanRq)
            result == []
    }

    def "Тест. Не найдена заявка по id при получении участников синдиката"(){
        given:
            def loanRqId = 11
        when:
            def result = participantService.getSyndicateParticipantsByRequestId(loanRqId)
        then:
            1 * loanRequestService.getById(loanRqId) >> Optional.empty()
            thrown(LoanRequestNotFoundException)
    }

    def "Тест. Сохранение коллекции участников"(){
        given:
            def participants = [Stub(SyndicateParticipant), Stub(SyndicateParticipant)]
        when:
            def savedPart = participantService.saveAll(participants)
        then:
            1 * participantRepository.saveAll(participants) >> participants
            participants == savedPart
    }

    static SyndicateParticipant createParticipant(long loanRequestId){
        def loanRequest = new LoanRequest()
        loanRequest.id = loanRequestId
        def syndicate = new Syndicate()
        syndicate.request = loanRequest
        def participant = new SyndicateParticipant()
        participant.syndicate = syndicate
        return participant
    }
}
