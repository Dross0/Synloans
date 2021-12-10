package com.synloans.loans.service

import com.synloans.loans.model.entity.LoanRequest
import com.synloans.loans.model.entity.Syndicate
import com.synloans.loans.repositories.SyndicateRepository
import spock.lang.Specification


class SyndicateServiceTest extends Specification {
    private SyndicateService syndicateService
    private SyndicateRepository syndicateRepository
    private LoanRequestService loanRequestService

    def setup(){
        syndicateRepository = Mock(SyndicateRepository)
        loanRequestService = Mock(LoanRequestService)
        syndicateService = new SyndicateService(syndicateRepository, loanRequestService)
    }

    def "Тест. Создание синдиката по id заявки"(){
        given:
            def loanRequestId = 101
            def loanRequestOp = Optional.of(Stub(LoanRequest))
        when:
            def createdSyndicate = syndicateService.getByLoanRequestId(loanRequestId, true)
        then:
            1 * syndicateRepository.findByRequest_Id(loanRequestId) >> null
            1 * loanRequestService.getById(loanRequestId) >> loanRequestOp
            1 * syndicateRepository.save(_ as Syndicate) >> {Syndicate syndicate -> syndicate}
            createdSyndicate.request == loanRequestOp.get()
    }

    def "Тест. Ошибка при создании синдиката по id заявки"(){
        given:
            def loanRequestId = 101
        when:
            def createdSyndicate = syndicateService.getByLoanRequestId(loanRequestId, true)
        then:
            1 * syndicateRepository.findByRequest_Id(loanRequestId) >> null
            1 * loanRequestService.getById(loanRequestId) >> Optional.empty()
            0 * syndicateRepository.save(_)
            thrown(IllegalArgumentException)
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

}
