package com.synloans.loans.mapper.converter.payments;

import com.synloans.loans.model.dto.loan.payments.ActualPaymentDto;
import com.synloans.loans.model.entity.loan.payment.ActualPayment;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ActualPaymentConverter implements Converter<ActualPayment, ActualPaymentDto> {

    @Override
    public ActualPaymentDto convert(ActualPayment actualPayment){
        return new ActualPaymentDto(
                actualPayment.getPayment(),
                actualPayment.getDate()
        );
    }

}
