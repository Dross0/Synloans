package com.sinloans.loans.repositories;

import com.sinloans.loans.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Company findByInnAndKpp(String inn, String kpp);

    boolean existsByInnAndKpp(String inn, String kpp);
}
