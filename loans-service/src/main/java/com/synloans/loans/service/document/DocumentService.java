package com.synloans.loans.service.document;

import com.synloans.loans.model.dto.document.DocumentDto;
import com.synloans.loans.model.dto.document.DocumentMetadata;
import com.synloans.loans.model.entity.document.Document;
import com.synloans.loans.model.entity.user.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DocumentService {
    Document save(Document document);

    Document findById(UUID documentId);

    Collection<Document> getAll();

    DocumentDto getDocument(UUID documentId, User user);

    DocumentDto getDocument(UUID documentId);

    DocumentMetadata upload(MultipartFile file, User user);

    List<DocumentMetadata> getAllUserDocuments(User user);

    boolean hasPermission(Document document, User user);
}
