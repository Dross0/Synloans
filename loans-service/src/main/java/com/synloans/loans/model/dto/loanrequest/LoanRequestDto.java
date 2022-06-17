package com.synloans.loans.model.dto.loanrequest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Getter
@EqualsAndHashCode
public class LoanRequestDto {
    @Positive(message = "Сумма кредита должна быть положительным числом")
    private final long sum;

    @PositiveOrZero(message = "Процентная ставка не может быть отрицательным числом")
    private final double maxRate;

    @Positive(message = "Срок кредитования должен быть положительным числом")
    private final int term;

    @JsonCreator
    public LoanRequestDto(
        @JsonProperty(value = "sum", required = true) long sum,
        @JsonProperty(value = "maxRate", required = true) double maxRate,
        @JsonProperty(value = "term", required = true) int term
    ){
        this.sum = sum;
        this.maxRate = maxRate;
        this.term = term;
    }
}
