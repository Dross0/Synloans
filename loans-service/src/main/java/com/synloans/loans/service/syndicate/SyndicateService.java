package com.synloans.loans.service.syndicate;

import com.synloans.loans.model.dto.SyndicateJoinRequest;
import com.synloans.loans.model.entity.Bank;
import com.synloans.loans.model.entity.LoanRequest;
import com.synloans.loans.model.entity.Syndicate;
import com.synloans.loans.model.entity.SyndicateParticipant;
import com.synloans.loans.repository.syndicate.SyndicateRepository;
import com.synloans.loans.service.exception.LoanRequestNotFoundException;
import com.synloans.loans.service.loan.LoanRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SyndicateService {
    private final SyndicateRepository syndicateRepository;
    private final LoanRequestService loanRequestService;
    private final SyndicateParticipantService syndicateParticipantService;

    public Syndicate getByLoanRequestId(Long loanRequestId){
        return syndicateRepository.findByRequest_Id(loanRequestId);
    }

    private Syndicate createSyndicateForLoanRequest(Long loanRequestId){
        LoanRequest loanRequest = loanRequestService.getById(loanRequestId).orElseThrow(LoanRequestNotFoundException::new);
        Syndicate syndicate = new Syndicate();
        syndicate.setRequest(loanRequest);
        return syndicateRepository.save(syndicate);
    }

    @Transactional
    public Optional<SyndicateParticipant> joinBankToSyndicate(SyndicateJoinRequest joinRequest, Bank bank) {
        Syndicate syndicate = getByLoanRequestId(joinRequest.getRequestId());
        if (syndicate == null){
            syndicate = createSyndicateForLoanRequest(joinRequest.getRequestId());
        }
        return Optional.ofNullable(syndicateParticipantService.createNewParticipant(
                syndicate,
                bank,
                joinRequest.getSum().getSum(),
                joinRequest.isApproveBankAgent()
        ));
    }
}
