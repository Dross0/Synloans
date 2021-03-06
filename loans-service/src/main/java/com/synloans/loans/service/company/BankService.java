package com.synloans.loans.service.company;

import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.document.Document;
import com.synloans.loans.repository.company.BankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class BankService {
    private final BankRepository bankRepository;

    public Bank createBank(Company company, Document license){
        Bank bank = new Bank();
        bank.setCompany(company);
        bank.setLicense(license);
        return bankRepository.save(bank);
    }

    public Bank createBank(Company company){
        return createBank(company, null);
    }

    public Bank save(Bank bank){
        return bankRepository.save(bank);
    }

    public Collection<Bank> getAll(){
        return bankRepository.findAll();
    }

    public Bank getById(Long id){
        return bankRepository.findById(id).orElse(null);
    }

    public Bank getByCompany(Company company) {
        return bankRepository.findByCompany(company);
    }
}
