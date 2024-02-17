package com.synloans.loans.repository.syndicate;

import com.synloans.loans.model.entity.syndicate.Syndicate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SyndicateRepository extends JpaRepository<Syndicate, Long> {
    Optional<Syndicate> findByRequest_Id(Long id);
}
