package com.synloans.loans.mapper.payment.schedule;

import com.synloans.loans.model.entity.loan.payment.PlannedPayment;
import com.synloans.loans.model.schedule.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;

@Mapper(componentModel = "spring")
@Component
public abstract class PaymentMapper {

    @Mapping(source = "paymentSum.percentPart", target = "percent", qualifiedByName = "convertMoney")
    @Mapping(source = "paymentSum.principalPart", target = "principal", qualifiedByName = "convertMoney")
    public abstract PlannedPayment convert(Payment payment);


    @Named("convertMoney")
    protected BigDecimal convertMoney(MonetaryAmount monetaryAmount) {
        if (monetaryAmount == null) {
            return null;
        }
        return monetaryAmount.getNumber().numberValue(BigDecimal.class);
    }
}
