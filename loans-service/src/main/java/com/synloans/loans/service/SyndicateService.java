package com.synloans.loans.service;

import com.synloans.loans.model.entity.LoanRequest;
import com.synloans.loans.model.entity.Syndicate;
import com.synloans.loans.repositories.SyndicateRepository;
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
        return syndicateRepository.save(syndicate);
    }
}
