package com.synloans.loans.mapper.converter.payments;

import com.synloans.loans.model.dto.loan.payments.PlannedPaymentDto;
import com.synloans.loans.model.entity.loan.payment.PlannedPayment;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PlannedPaymentConverter implements Converter<PlannedPayment, PlannedPaymentDto> {

    @Override
    public PlannedPaymentDto convert(PlannedPayment plannedPayment) {
        return new PlannedPaymentDto(
                plannedPayment.getPrincipal(),
                plannedPayment.getPercent(),
                plannedPayment.getDate()
        );
    }
}
