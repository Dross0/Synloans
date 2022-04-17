package com.synloans.loans.mapper;

import com.synloans.loans.model.dto.collection.LoanRequestCollection;
import com.synloans.loans.model.dto.collection.LoanRequestCollectionResponse;
import com.synloans.loans.model.dto.loanrequest.LoanRequestResponse;
import com.synloans.loans.model.entity.loan.LoanRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LoanRequestCollectionConverter.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LoanRequestCollectionConverterTest {

    @Autowired
    private LoanRequestCollectionConverter requestCollectionConverter;

    @MockBean
    Converter<LoanRequest, LoanRequestResponse> loanRequestConverter;

    @Test
    @DisplayName("Тест. Конвертация коллекции заявок на кредит")
    void convertLoanRequestsListTest(){
        LoanRequestCollection loanRequestCollection = new LoanRequestCollection();

        LoanRequest own1 = new LoanRequest();
        LoanRequest own2 = new LoanRequest();

        LoanRequest other1 = new LoanRequest();
        LoanRequest other2 = new LoanRequest();

        loanRequestCollection.addOwn(own1);
        loanRequestCollection.addOwn(own2);
        loanRequestCollection.addOther(other1);
        loanRequestCollection.addOther(other2);

        LoanRequestResponse own1Response = new LoanRequestResponse();
        LoanRequestResponse own2Response = new LoanRequestResponse();
        LoanRequestResponse other1Response = new LoanRequestResponse();
        LoanRequestResponse other2Response = new LoanRequestResponse();

        when(loanRequestConverter.convert(own1)).thenReturn(own1Response);
        when(loanRequestConverter.convert(own2)).thenReturn(own2Response);
        when(loanRequestConverter.convert(other1)).thenReturn(other1Response);
        when(loanRequestConverter.convert(other2)).thenReturn(other2Response);

        LoanRequestCollectionResponse collectionResponse = requestCollectionConverter.convert(loanRequestCollection);

        assertThat(collectionResponse).isNotNull();

        assertThat(collectionResponse.getOwned())
                .hasSize(2)
                .contains(own1Response, own2Response);

        assertThat(collectionResponse.getOther())
                .hasSize(2)
                .contains(other1Response, other2Response);

        verify(loanRequestConverter, times(1)).convert(own1);
        verify(loanRequestConverter, times(1)).convert(own2);
        verify(loanRequestConverter, times(1)).convert(other1);
        verify(loanRequestConverter, times(1)).convert(other2);

    }
}