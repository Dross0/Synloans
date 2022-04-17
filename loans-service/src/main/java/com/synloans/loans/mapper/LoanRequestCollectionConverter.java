package com.synloans.loans.mapper;

import com.synloans.loans.model.dto.collection.LoanRequestCollection;
import com.synloans.loans.model.dto.collection.LoanRequestCollectionResponse;
import com.synloans.loans.model.dto.loanrequest.LoanRequestResponse;
import com.synloans.loans.model.entity.loan.LoanRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanRequestCollectionConverter implements Converter<LoanRequestCollection, LoanRequestCollectionResponse> {

    private final Converter<LoanRequest, LoanRequestResponse> loanRequestConverter;

    @Override
    public LoanRequestCollectionResponse convert(LoanRequestCollection source) {
        LoanRequestCollectionResponse collectionResponse = new LoanRequestCollectionResponse();
        for (LoanRequest loanRequest: source.getOwnRequests()){
            collectionResponse.addOwn(loanRequestConverter.convert(loanRequest));
        }
        for (LoanRequest loanRequest: source.getOtherRequests()){
            collectionResponse.addOther(loanRequestConverter.convert(loanRequest));
        }
        return collectionResponse;
    }

}
