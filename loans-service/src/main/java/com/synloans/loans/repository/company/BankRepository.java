package com.synloans.loans.repository.company;

import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.model.entity.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {
    Optional<Bank> findByCompany(Company company);
}
