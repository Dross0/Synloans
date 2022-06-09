package com.synloans.loans.model.payment;

import lombok.NonNull;
import lombok.Value;
import org.javamoney.moneta.Money;

@Value
public class PaymentSum {
    @NonNull Money principalPart;
    @NonNull Money percentPart;

    public Money getAmount(){
        return principalPart.add(percentPart);
    }
}
