package com.synloans.loans.repositories;

import com.synloans.loans.model.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Company findByInnAndKpp(String inn, String kpp);

    boolean existsByInnAndKpp(String inn, String kpp);
}
