package com.synloans.loans.model.dto.loan.payments;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class PaymentRequest {

    @Positive(message = "Сумма платежа не может быть нулевой или отрицательной")
    private long payment;

    @PastOrPresent(message = "Дата платежа не может быть позже текущей")
    @JsonSetter(nulls = Nulls.SKIP)
    private LocalDate date = LocalDate.now();

}
