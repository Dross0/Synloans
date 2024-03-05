package com.synloans.loans.controller.document;

import com.synloans.loans.model.dto.document.DocumentMetadata;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DocumentController {

    DocumentMetadata uploadDocument(MultipartFile file);

    ResponseEntity<Resource> readDocument(UUID documentId);

    List<DocumentMetadata> getDocuments();
}
