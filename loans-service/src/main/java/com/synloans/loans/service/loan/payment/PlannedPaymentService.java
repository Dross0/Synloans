package com.synloans.loans.service.loan.payment;

import com.synloans.loans.model.entity.PlannedPayment;
import com.synloans.loans.repository.loan.payment.PlannedPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class PlannedPaymentService {
    private final PlannedPaymentRepository paymentRepository;

    public PlannedPayment save(PlannedPayment payment){
        return paymentRepository.save(payment);
    }

    public Collection<PlannedPayment> save(Collection<PlannedPayment> payments){
        return paymentRepository.saveAll(payments);
    }

    public PlannedPayment getById(Long id){
        return paymentRepository.findById(id).orElse(null);
    }
}
