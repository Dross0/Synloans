package com.synloans.loans.model.dto.loanrequest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RequiredArgsConstructor
@Getter
@Setter
public class LoanRequestDto {
    @Positive(message = "Сумма кредита должна быть положительным числом")
    private long sum;

    @PositiveOrZero(message = "Процентная ставка не может быть отрицательным числом")
    private double maxRate;

    @Positive(message = "Срок кредитования должен быть положительным числом")
    private int term;
}
