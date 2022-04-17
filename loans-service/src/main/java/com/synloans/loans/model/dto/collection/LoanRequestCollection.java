package com.synloans.loans.model.dto.collection;

import com.synloans.loans.model.entity.loan.LoanRequest;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class LoanRequestCollection {

    private final Collection<LoanRequest> ownRequests;

    private final Collection<LoanRequest> otherRequests;

    public LoanRequestCollection(){
        this.ownRequests = new HashSet<>();
        this.otherRequests = new HashSet<>();
    }

    public void addOwn(LoanRequest loanRequest){
        ownRequests.add(loanRequest);
    }

    public void addOther(LoanRequest loanRequest){
        otherRequests.add(loanRequest);
    }

    public Collection<LoanRequest> getOwnRequests(){
        return Collections.unmodifiableCollection(ownRequests);
    }

    public Collection<LoanRequest> getOtherRequests(){
        return Collections.unmodifiableCollection(otherRequests);
    }
}
