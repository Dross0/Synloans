package com.sinloans.loans.service;

import com.sinloans.loans.model.Company;
import com.sinloans.loans.repositories.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CompanyService {
    private static final Logger log = LoggerFactory.getLogger(CompanyService.class);

    private final CompanyRepository companyRepository;

    public Optional<Company> getById(Long id){
        return companyRepository.findById(id);
    }

    public Collection<Company> getAll(){
        return companyRepository.findAll();
    }

    public Optional<Company> getByInnAndKpp(String inn, String kpp){
        return Optional.ofNullable(companyRepository.findByInnAndKpp(inn, kpp));
    }

    public Company create(Company company){
        if (companyRepository.existsByInnAndKpp(company.getInn(), company.getKpp())){
            log.error("Компания с инн={} и кпп={} существует", company.getInn(), company.getKpp());
            return null;
        }
        return companyRepository.save(company);
    }

    public Company save(Company company){
        return companyRepository.save(company);
    }
}
