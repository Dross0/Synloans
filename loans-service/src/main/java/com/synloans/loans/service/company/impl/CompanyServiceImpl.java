package com.synloans.loans.service.company.impl;

import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.repository.company.CompanyRepository;
import com.synloans.loans.service.company.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;

    @Override
    public Optional<Company> getById(Long id){
        return companyRepository.findById(id);
    }

    @Override
    public Collection<Company> getAll(){
        return companyRepository.findAll();
    }

    @Override
    public Optional<Company> getByInnAndKpp(String inn, String kpp){
        return Optional.ofNullable(companyRepository.findByInnAndKpp(inn, kpp));
    }

    @Transactional
    @Override
    public Company create(Company company){
        if (companyRepository.existsByInnAndKpp(company.getInn(), company.getKpp())){
            log.error("Компания с инн={} и кпп={} существует", company.getInn(), company.getKpp());
            return null;
        }
        return companyRepository.save(company);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Company save(Company company){
        return companyRepository.save(company);
    }
}
