package com.synloans.loans.service.bank.impl;

import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.document.Document;
import com.synloans.loans.repository.company.BankRepository;
import com.synloans.loans.service.bank.BankService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BankServiceImpl implements BankService {
    private final BankRepository bankRepository;

    @Override
    public Bank createBank(Company company, Document license){
        Bank bank = new Bank(); //TODO if bank with that company already exist?
        bank.setCompany(company);
        bank.setLicense(license);
        return bankRepository.save(bank);
    }

    @Override
    public Bank createBank(Company company){
        return createBank(company, null);
    }

    @Override
    public Bank save(Bank bank){
        return bankRepository.save(bank);
    }

    @Override
    public Collection<Bank> getAll(){
        return bankRepository.findAll();
    }

    @Override
    public Optional<Bank> getById(long id){
        return bankRepository.findById(id);
    }

    @Override
    public Optional<Bank> getByCompany(Company company) {
        return bankRepository.findByCompany(company);
    }
}
