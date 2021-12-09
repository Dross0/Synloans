package com.synloans.loans.repositories;

import com.synloans.loans.model.entity.Syndicate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyndicateRepository extends JpaRepository<Syndicate, Long> {
    Syndicate findByRequest_Id(Long id);
}