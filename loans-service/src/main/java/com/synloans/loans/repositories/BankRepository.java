package com.synloans.loans.repositories;

import com.synloans.loans.model.entity.Bank;
import com.synloans.loans.model.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {
    Bank findByCompany(Company company);
}
