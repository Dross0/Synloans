package com.synloans.loans.service

import com.synloans.loans.model.entity.Bank
import com.synloans.loans.model.entity.LoanRequest
import com.synloans.loans.model.entity.Syndicate
import com.synloans.loans.model.entity.SyndicateParticipant
import com.synloans.loans.repositories.SyndicateParticipantRepository
import spock.lang.Specification

class SyndicateParticipantServiceTest extends Specification{
    private SyndicateParticipantService participantService
    private SyndicateParticipantRepository participantRepository

    def setup(){
        participantRepository = Mock(SyndicateParticipantRepository)
        participantService = new SyndicateParticipantService(participantRepository)
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
    }

    def "Тест. Выход участника из синдиката"(){
        given:
            def idToDelete = 103
            def participantToDelete = createParticipantStub(idToDelete)
            Set<SyndicateParticipant> participants = [
                    createParticipantStub(1),
                    participantToDelete,
                    createParticipantStub(3),
                    createParticipantStub(111)
            ] as Set
            def bank = Stub(Bank)
            bank.syndicates >> participants
        when:
            participantService.quitFromSyndicate(idToDelete, bank)
        then:
            1 * participantRepository.delete(participantToDelete)



    }

    SyndicateParticipant createParticipantStub(long loanRequestId){
        def loanRequest = Stub(LoanRequest)
        loanRequest.getId() >> loanRequestId
        def syndicate = Stub(Syndicate)
        syndicate.getRequest() >> loanRequest
        def participant = Stub(SyndicateParticipant)
        participant.getSyndicate() >> syndicate
        return participant
    }
}
