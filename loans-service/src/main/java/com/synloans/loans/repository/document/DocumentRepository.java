package com.synloans.loans.repository.document;

import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.document.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findAllByOwnerCompany(Company company);

}
