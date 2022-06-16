package com.synloans.loans.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.money.MonetaryAmount;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Schema(description = "Условия кредита")
@Getter
@EqualsAndHashCode
public class LoanTerms {

    @Schema(description = "Сумма кредита")
    @NotNull(message = "Loan sum must be not null")
    @PositiveOrZero(message = "Loan sum must be positive or zero")
    private final MonetaryAmount loanSum;

    @Schema(description = "Процентная ставка по кредиту", example = "0.12")
    @PositiveOrZero(message = "Loan rate must be positive or zero")
    private final BigDecimal rate;

    @Schema(description = "Дата взятия кредита")
    @NotNull(message = "Loan issue date must be not null")
    private final LocalDate issueDate;

    @Schema(description = "Срок кредитования в месяцах")
    @Positive(message = "Months must be positive")
    private final int months;

    @JsonCreator
    public LoanTerms(
            @JsonProperty(value = "loanSum", required = true) MonetaryAmount loanSum,
            @JsonProperty(value = "rate", required = true) BigDecimal rate,
            @JsonProperty(value = "issueDate", required = true) LocalDate issueDate,
            @JsonProperty(value = "months") Integer months
    ) {
        this.loanSum = loanSum;
        this.rate = rate;
        this.months = Objects.requireNonNullElse(months, 0);
        this.issueDate = issueDate;
    }

}
