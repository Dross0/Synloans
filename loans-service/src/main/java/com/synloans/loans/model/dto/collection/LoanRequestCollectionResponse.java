package com.synloans.loans.model.dto.collection;

import com.synloans.loans.model.dto.loanrequest.LoanRequestResponse;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;


public class LoanRequestCollectionResponse {

    private final Collection<LoanRequestResponse> own;

    private final Collection<LoanRequestResponse> other;

    public LoanRequestCollectionResponse(){
        this.own = new HashSet<>();
        this.other = new HashSet<>();
    }

    public void addOwn(LoanRequestResponse loanRequestResponse){
        own.add(loanRequestResponse);
    }

    public void addOther(LoanRequestResponse loanRequestResponse){
        other.add(loanRequestResponse);
    }

    public Collection<LoanRequestResponse> getOwn(){
        return Collections.unmodifiableCollection(own);
    }

    public Collection<LoanRequestResponse> getOther(){
        return Collections.unmodifiableCollection(other);
    }

}
