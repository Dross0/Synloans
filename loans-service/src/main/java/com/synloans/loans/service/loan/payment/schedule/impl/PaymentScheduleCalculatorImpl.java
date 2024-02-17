package com.synloans.loans.service.loan.payment.schedule.impl;

import com.synloans.loans.client.PaymentsCalculatorClient;
import com.synloans.loans.mapper.payment.schedule.LoanTermsMapper;
import com.synloans.loans.mapper.payment.schedule.PaymentMapper;
import com.synloans.loans.model.entity.loan.Loan;
import com.synloans.loans.model.entity.loan.payment.PlannedPayment;
import com.synloans.loans.model.schedule.LoanTerms;
import com.synloans.loans.model.schedule.Payment;
import com.synloans.loans.service.exception.PaymentsScheduleCalculationException;
import com.synloans.loans.service.loan.payment.schedule.PaymentScheduleCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentScheduleCalculatorImpl implements PaymentScheduleCalculator {

    private final PaymentsCalculatorClient paymentsCalculatorClient;
    private final LoanTermsMapper loanTermsMapper;
    private final PaymentMapper paymentMapper;
    @Override
    public List<PlannedPayment> calculatePlannedPayments(Loan loan) {
        LoanTerms loanTerms = loanTermsMapper.convert(loan);

        try {
            List<Payment> payments = paymentsCalculatorClient.calculateAnnuityPaymentSchedule(loanTerms);
            return payments.stream()
                    .map(paymentMapper::convert)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error while calculate payments schedule for loan with id={}", loan.getId(), e);
            throw new PaymentsScheduleCalculationException("Failed calculate payments schedule for loan with id=" + loan.getId());
        }
    }
}
