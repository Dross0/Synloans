package com.synloans.loans.model.schedule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.money.MonetaryAmount;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

@RequiredArgsConstructor
@Getter
@Setter
public class LoanTerms {

    @PositiveOrZero(message = "Сумма кредита должна быть больше или равна 0")
    private MonetaryAmount loanSum;

    @PositiveOrZero(message = "Процентная ставка должна быть больше или равна 0")
    private BigDecimal rate;

    @NotNull(message = "Дата взятия кредита должна присутсвовать")
    private LocalDate issueDate;

    @Positive(message = "Количество месяцев должно быть больше 0")
    private int months;
}
