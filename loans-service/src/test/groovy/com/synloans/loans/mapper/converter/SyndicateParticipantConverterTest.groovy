package com.synloans.loans.mapper.converter

import com.synloans.loans.model.dto.BankParticipantInfo
import com.synloans.loans.model.dto.LoanSum
import com.synloans.loans.model.entity.company.Bank
import com.synloans.loans.model.entity.company.Company
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant
import spock.lang.Specification

class SyndicateParticipantConverterTest extends Specification{

    private SyndicateParticipantConverter participantConverter

    def setup(){
        participantConverter = new SyndicateParticipantConverter()
    }

    def "Тест. Участник синдиката в информацию о банке"(){
        given:
            Company company = new Company()
            company.fullName = "Company Co."

            Bank bank = new Bank()
            bank.id = 14
            bank.company = company

            SyndicateParticipant syndicateParticipant = new SyndicateParticipant()
            syndicateParticipant.id = 12
            syndicateParticipant.loanSum = 102L
            syndicateParticipant.approveBankAgent = true
            syndicateParticipant.bank = bank

        when:
            BankParticipantInfo bankParticipantInfo = participantConverter.convert(syndicateParticipant)
        then:
            verifyAll(bankParticipantInfo){
                id == bank.id
                name == company.fullName
                sum == LoanSum.valueOf(syndicateParticipant.loanSum)
                approveBankAgent == syndicateParticipant.approveBankAgent
            }
    }
}
