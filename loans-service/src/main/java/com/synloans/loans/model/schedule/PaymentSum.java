package com.synloans.loans.model.schedule;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.money.MonetaryAmount;
import javax.validation.constraints.PositiveOrZero;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class PaymentSum {

    @PositiveOrZero(message = "Сумма оплаты основного долга должна быть больше или равна 0")
    private final MonetaryAmount principalPart;

    @PositiveOrZero(message = "Сумма оплаты процентнов должна быть больше или равна 0")
    private final MonetaryAmount percentPart;
}
