package com.synloans.loans.service.document;

import com.synloans.loans.model.entity.document.Document;

import java.util.Collection;
import java.util.Optional;

public interface DocumentService {
    Document save(Document document);

    Collection<Document> getAll();

    Optional<Document> getById(long id);
}
