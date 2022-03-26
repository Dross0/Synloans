package com.synloans.loans.service.loan.payment;

import com.synloans.loans.model.dto.loan.payments.PaymentRequest;
import com.synloans.loans.model.entity.loan.Loan;
import com.synloans.loans.model.entity.loan.payment.ActualPayment;
import com.synloans.loans.repository.loan.payment.ActualPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ActualPaymentService {
    private final ActualPaymentRepository paymentRepository;

    public ActualPayment save(ActualPayment payment){
        return paymentRepository.save(payment);
    }

    @Transactional
    public ActualPayment createPayment(Loan loan, PaymentRequest paymentRequest){
        ActualPayment actualPayment = new ActualPayment();
        actualPayment.setPayment(paymentRequest.getPayment());
        actualPayment.setDate(paymentRequest.getDate());
        actualPayment.setLoan(loan);
        loan.getActualPayments().add(actualPayment);
        return paymentRepository.save(actualPayment);
    }

    public Collection<ActualPayment> save(Collection<ActualPayment> payments){
        return paymentRepository.saveAll(payments);
    }

    public ActualPayment getById(Long id){
        return paymentRepository.findById(id).orElse(null);
    }
}
