package com.synloans.loans.service.document;

import com.synloans.loans.model.entity.Document;
import com.synloans.loans.repositories.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;

    public Document save(Document document){
        return documentRepository.save(document);
    }

    public Collection<Document> getAll(){
        return documentRepository.findAll();
    }

    public Document getById(Long id){
        return documentRepository.findById(id).orElse(null);
    }
}
