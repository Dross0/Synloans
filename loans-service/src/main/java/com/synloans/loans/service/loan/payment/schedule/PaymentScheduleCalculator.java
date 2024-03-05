package com.synloans.loans.service.loan.payment.schedule;

import com.synloans.loans.model.entity.loan.Loan;
import com.synloans.loans.model.entity.loan.payment.PlannedPayment;

import java.util.List;

public interface PaymentScheduleCalculator {

    List<PlannedPayment> calculatePlannedPayments(Loan loan);

}
