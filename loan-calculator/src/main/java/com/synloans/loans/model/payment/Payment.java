package com.synloans.loans.model.payment;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.money.MonetaryAmount;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

@Schema(description = "Информация о платеже по кредиту")
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class Payment {

    @Schema(description = "Сумма платежа")
    @NotNull(message = "Сумма платежа должна присутствовать")
    private final PaymentSum paymentSum;

    @Schema(description = "Остаток основного долга после совершения платежа")
    @PositiveOrZero(message = "Остаток основного долга должен быть больше или равен 0")
    private final MonetaryAmount loanBalance;

    @Schema(description = "Дата платежа")
    @NotNull(message = "Дата платежа должна присутствовать")
    private final LocalDate date;
}
