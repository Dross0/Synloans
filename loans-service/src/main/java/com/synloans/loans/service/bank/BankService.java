package com.synloans.loans.service.bank;

import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.document.Document;

import java.util.Collection;
import java.util.Optional;

public interface BankService {
    Bank createBank(Company company, Document license);

    Bank createBank(Company company);

    Bank save(Bank bank);

    Collection<Bank> getAll();

    Optional<Bank> getById(long id);

    Optional<Bank> getByCompany(Company company);
}
