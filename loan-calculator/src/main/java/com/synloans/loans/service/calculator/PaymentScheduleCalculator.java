package com.synloans.loans.service.calculator;

import com.synloans.loans.model.LoanTerms;
import com.synloans.loans.model.payment.Payment;

import java.util.List;

public interface PaymentScheduleCalculator {

    List<Payment> calculateSchedule(LoanTerms loanTerms);

}
