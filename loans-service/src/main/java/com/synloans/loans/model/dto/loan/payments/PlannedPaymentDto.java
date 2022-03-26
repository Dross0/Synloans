package com.synloans.loans.model.dto.loan.payments;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@RequiredArgsConstructor
@Getter
public class PlannedPaymentDto {
    private final BigDecimal principal;

    private final BigDecimal percent;

    private final LocalDate date;
}
