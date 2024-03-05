package com.synloans.loans.service.syndicate.impl;

import com.synloans.loans.model.dto.SyndicateJoinRequest;
import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.model.entity.loan.LoanRequest;
import com.synloans.loans.model.entity.syndicate.Syndicate;
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant;
import com.synloans.loans.repository.syndicate.SyndicateRepository;
import com.synloans.loans.service.exception.notfound.LoanRequestNotFoundException;
import com.synloans.loans.service.loan.LoanRequestService;
import com.synloans.loans.service.syndicate.SyndicateService;
import com.synloans.loans.service.syndicate.participant.SyndicateParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyndicateServiceImpl implements SyndicateService {
    private final SyndicateRepository syndicateRepository;
    private final LoanRequestService loanRequestService;
    private final SyndicateParticipantService syndicateParticipantService;

    @Override
    public Optional<Syndicate> getByLoanRequestId(long loanRequestId){
        return syndicateRepository.findByRequest_Id(loanRequestId);
    }

    private Syndicate createSyndicateForLoanRequest(Long loanRequestId){
        LoanRequest loanRequest = loanRequestService.getById(loanRequestId)
                .orElseThrow(() ->
                        new LoanRequestNotFoundException("Заявка на кредит с id=" + loanRequestId +" не найдена")
                );
        Syndicate syndicate = new Syndicate();
        syndicate.setRequest(loanRequest);
        return syndicateRepository.save(syndicate);
    }

    @Transactional
    @Override
    public Optional<SyndicateParticipant> joinBankToSyndicate(SyndicateJoinRequest joinRequest, Bank bank) {
        Syndicate syndicate = getByLoanRequestId(joinRequest.getRequestId())
                .orElseGet(() -> createSyndicateForLoanRequest(joinRequest.getRequestId()));
        if (syndicate.getRequest().getLoan() != null){
            log.error("Попытка банка={} присоединиться в синдикат по заявке с id={}, когда кредит уже выдан",
                    bank.getCompany().getFullName(),
                    joinRequest.getRequestId()
            );
            return Optional.empty();
        }
        return Optional.ofNullable(syndicateParticipantService.createNewParticipant(
                syndicate,
                bank,
                joinRequest.getSum(),
                joinRequest.isApproveBankAgent()
        ));
    }
}
