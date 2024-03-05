package com.synloans.loans.repository.document;

import com.synloans.loans.model.entity.document.Contract;
import com.synloans.loans.model.entity.document.ContractStatus;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    String SKIP_LOCKED = "-2";

    @QueryHints(@QueryHint(name = AvailableSettings.JPA_LOCK_TIMEOUT, value = SKIP_LOCKED))
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Contract> findByStatus(ContractStatus contractStatus, Pageable pageable);

}
