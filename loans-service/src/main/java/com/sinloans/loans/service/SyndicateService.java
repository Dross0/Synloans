package com.sinloans.loans.service;

import com.sinloans.loans.model.dto.LoanSum;
import com.sinloans.loans.model.entity.Bank;
import com.sinloans.loans.model.entity.LoanRequest;
import com.sinloans.loans.model.entity.Syndicate;
import com.sinloans.loans.model.entity.SyndicateParticipant;
import com.sinloans.loans.repositories.SyndicateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyndicateService {
    private final SyndicateRepository syndicateRepository;
    private final LoanRequestService loanRequestService;

    public Syndicate getByLoanRequestId(Long loanRequestId, boolean createIfNoExist){
        Syndicate syndicate = syndicateRepository.findByRequest_Id(loanRequestId);
        if (syndicate == null && createIfNoExist){
            syndicate = createSyndicateForLoanRequest(loanRequestId);
        }
        return syndicate;
    }

    public Syndicate getByLoanRequestId(Long loanRequestId){
        return getByLoanRequestId(loanRequestId, false);
    }

    private Syndicate createSyndicateForLoanRequest(Long loanRequestId){
        LoanRequest loanRequest = loanRequestService.getById(loanRequestId).orElseThrow(IllegalArgumentException::new);
        Syndicate syndicate = new Syndicate();
        syndicate.setRequest(loanRequest);
        syndicateRepository.save(syndicate);
        return syndicate;
    }
}
