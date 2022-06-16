package com.synloans.loans.controller;

import com.synloans.loans.model.LoanTerms;
import com.synloans.loans.model.payment.Payment;

import java.util.List;

public interface PaymentScheduleController {

    List<Payment> calculateAnnuityPaymentSchedule(LoanTerms loanTerms);

    List<Payment> calculateDifferentiatedPaymentSchedule(LoanTerms loanTerms);

}
