package com.synloans.loans.repository.loan.payment;

import com.synloans.loans.model.entity.loan.payment.ActualPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActualPaymentRepository extends JpaRepository<ActualPayment, Long> {
}
