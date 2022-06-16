package com.synloans.loans.model.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.money.MonetaryAmount;
import javax.validation.constraints.NotNull;

@Schema(description = "Сумма платежа по кредиту")
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class PaymentSum {

    @Schema(description = "Сумма оплаты основного долга")
    @NotNull
    private final MonetaryAmount principalPart;

    @Schema(description = "Сумма оплаты процентной части")
    @NotNull
    private final MonetaryAmount percentPart;
}
