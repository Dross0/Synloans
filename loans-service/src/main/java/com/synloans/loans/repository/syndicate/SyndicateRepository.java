package com.synloans.loans.repository.syndicate;

import com.synloans.loans.model.entity.syndicate.Syndicate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyndicateRepository extends JpaRepository<Syndicate, Long> {
    Syndicate findByRequest_Id(Long id);
}
