package com.synloans.loans.model.dto.loan.payments;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@RequiredArgsConstructor
@Getter
public class ActualPaymentDto {

    private final long payment;

    private final LocalDate date;

}
