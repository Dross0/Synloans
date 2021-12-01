package com.sinloans.loans.repositories;

import com.sinloans.loans.model.Bank;
import com.sinloans.loans.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {
    Bank findByCompany(Company company);
}
