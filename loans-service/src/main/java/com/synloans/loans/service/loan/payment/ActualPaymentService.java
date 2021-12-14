package com.synloans.loans.service.loan.payment;

import com.synloans.loans.model.entity.loan.payment.ActualPayment;
import com.synloans.loans.repository.loan.payment.ActualPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ActualPaymentService {
    private final ActualPaymentRepository paymentRepository;

    public ActualPayment save(ActualPayment payment){
        return paymentRepository.save(payment);
    }

    public Collection<ActualPayment> save(Collection<ActualPayment> payments){
        return paymentRepository.saveAll(payments);
    }

    public ActualPayment getById(Long id){
        return paymentRepository.findById(id).orElse(null);
    }
}
