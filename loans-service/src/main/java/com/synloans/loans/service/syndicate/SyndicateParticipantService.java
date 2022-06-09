package com.synloans.loans.service.syndicate;

import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.model.entity.loan.Loan;
import com.synloans.loans.model.entity.loan.LoanRequest;
import com.synloans.loans.model.entity.syndicate.Syndicate;
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant;
import com.synloans.loans.repository.syndicate.SyndicateParticipantRepository;
import com.synloans.loans.service.exception.SyndicateQuitException;
import com.synloans.loans.service.exception.notfound.LoanRequestNotFoundException;
import com.synloans.loans.service.loan.LoanRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SyndicateParticipantService {
    private final SyndicateParticipantRepository participantRepository;
    private final LoanRequestService loanRequestService;

    @Transactional
    public SyndicateParticipant createNewParticipant(Syndicate syndicate, Bank bank, long loanSum, boolean approveBankAgent){
        SyndicateParticipant participant = new SyndicateParticipant();
        participant.setApproveBankAgent(approveBankAgent);
        participant.setSyndicate(syndicate);
        participant.setBank(bank);
        participant.setLoanSum(loanSum);
        participant.setIssuedLoanSum(null);
        return participantRepository.save(participant);
    }

    @Transactional
    public List<SyndicateParticipant> saveAll(List<SyndicateParticipant> syndicateParticipants){
        return participantRepository.saveAll(syndicateParticipants);
    }

    @Transactional
    public void quitFromSyndicate(Long loanRequestId, Bank bank) {
        for (SyndicateParticipant syndicateParticipant: bank.getSyndicates()){
            if (Objects.equals(syndicateParticipant.getSyndicate().getRequest().getId(), loanRequestId)){
                Loan loan = syndicateParticipant.getSyndicate().getRequest().getLoan();
                if (loan != null){
                    throw new SyndicateQuitException("Нельзя выйти из синдиката, когда кредит уже выдан");
                }
                participantRepository.delete(syndicateParticipant);
                return;
            }
        }
    }

    public Collection<SyndicateParticipant> getSyndicateParticipantsByRequestId(long id){
        LoanRequest loanRequest = loanRequestService
                .getById(id)
                .orElseThrow(
                        () -> {throw new LoanRequestNotFoundException("Заявка с id = " + id + " не найдена");}
                );
        Syndicate syndicate = loanRequest.getSyndicate();
        if (syndicate == null){
            return Collections.emptyList();
        }
        return syndicate.getParticipants();
    }
}
