package com.synloans.loans.model.dto.loan.payments;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@EqualsAndHashCode
public class PaymentRequest {

    @Positive(message = "Сумма платежа не может быть нулевой или отрицательной")
    private final long payment;

    @PastOrPresent(message = "Дата платежа не может быть позже текущей")
    @JsonSetter(nulls = Nulls.SKIP)
    private final LocalDate date;

    @JsonCreator
    public PaymentRequest(
            @JsonProperty(value = "payment", required = true) long payment,
            @JsonProperty("date") LocalDate date
    ) {
        this.payment = payment;
        this.date = Objects.requireNonNullElse(date, LocalDate.now());
    }

}
