package com.synloans.loans.service.document.impl;

import com.synloans.loans.model.entity.document.Document;
import com.synloans.loans.repository.document.DocumentRepository;
import com.synloans.loans.service.document.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;

    @Override
    public Document save(Document document){
        return documentRepository.save(document);
    }

    @Override
    public Collection<Document> getAll(){
        return documentRepository.findAll();
    }

    @Override
    public Optional<Document> getById(long id){
        return documentRepository.findById(id);
    }
}
