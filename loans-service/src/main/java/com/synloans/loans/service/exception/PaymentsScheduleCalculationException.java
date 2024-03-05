package com.synloans.loans.service.exception;

public class PaymentsScheduleCalculationException extends RuntimeException {
    public PaymentsScheduleCalculationException() {
    }

    public PaymentsScheduleCalculationException(String message) {
        super(message);
    }

    public PaymentsScheduleCalculationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymentsScheduleCalculationException(Throwable cause) {
        super(cause);
    }
}
