package com.synloans.loans.model.schedule;


import lombok.Getter;
import lombok.Setter;

import javax.money.MonetaryAmount;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

@Getter
@Setter
public class Payment {

    @NotNull(message = "Сумма платежа должна присутствовать")
    private PaymentSum paymentSum;

    @PositiveOrZero(message = "Остаток основного долга должен быть больше или равен 0")
    private MonetaryAmount loanBalance;

    @NotNull(message = "Дата платежа должна присутствовать")
    private LocalDate date;
}
