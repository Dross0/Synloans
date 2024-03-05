package com.synloans.loans.service.company;

import com.synloans.loans.model.entity.company.Company;

import java.util.Collection;
import java.util.Optional;

public interface CompanyService {
    Optional<Company> getById(Long id);

    Collection<Company> getAll();

    Optional<Company> getByInnAndKpp(String inn, String kpp);

    Company create(Company company);

    Company save(Company company);
}
