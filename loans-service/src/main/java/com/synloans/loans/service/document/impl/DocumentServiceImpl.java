package com.synloans.loans.service.document.impl;

import com.synloans.loans.mapper.document.DocumentMapper;
import com.synloans.loans.mapper.document.DocumentMetadataMapper;
import com.synloans.loans.model.dto.document.DocumentDto;
import com.synloans.loans.model.dto.document.DocumentMetadata;
import com.synloans.loans.model.entity.document.Document;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.repository.document.DocumentRepository;
import com.synloans.loans.service.document.DocumentService;
import com.synloans.loans.service.exception.ForbiddenResourceException;
import com.synloans.loans.service.exception.document.DocumentNotFoundException;
import com.synloans.loans.service.exception.document.DocumentUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;

    private final DocumentMetadataMapper documentMetadataMapper;

    private final DocumentMapper documentMapper;

    @Override
    public Document save(Document document){
        return documentRepository.save(document);
    }

    @Override
    public Document findById(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("No document with id=" + documentId));
    }

    @Override
    public Collection<Document> getAll(){
        return documentRepository.findAll();
    }

    @Override
    public DocumentMetadata upload(MultipartFile file, User user) {
        Instant createdDate = Instant.now();
        UUID documentId = UUID.randomUUID();
        try {
            log.info("Upload document with id={} and name={} by user={}", documentId, file.getOriginalFilename(), user);
            Document document = Document.builder()
                    .id(documentId)
                    .owner(user)
                    .name(file.getOriginalFilename())
                    .createDate(createdDate)
                    .lastUpdate(createdDate)
                    .body(file.getBytes())
                    .build();
            document = save(document);
            return documentMetadataMapper.convert(document);
        } catch (IOException e) {
            log.error("Error while reading document with name={} by user={}", file.getOriginalFilename(), user, e);
            throw new DocumentUploadException("Error while file saving", e);
        }
    }

    @Transactional
    @Override
    public DocumentDto getDocument(UUID documentId, User user) {
        Document document = findById(documentId);
        validateOwner(document, user);
        return documentMapper.convert(document);
    }

    @Transactional
    @Override
    public DocumentDto getDocument(UUID documentId) {
        Document document = findById(documentId);
        return documentMapper.convert(document);
    }

    @Override
    public List<DocumentMetadata> getAllUserDocuments(User user) {
        return documentRepository.findAllByOwnerCompany(user.getCompany())
                .stream()
                .map(documentMetadataMapper::convert)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasPermission(Document document, User user) {
        return Objects.equals(document.getOwner().getCompany().getId(), user.getCompany().getId());
    }

    private void validateOwner(Document document, User user) {
        if (!hasPermission(document, user)) {
            throw new ForbiddenResourceException("Нет доступа для просмотра документа");
        }
    }
}
