package com.synloans.loans.controller.document.impl;

import com.synloans.loans.configuration.api.Api;
import com.synloans.loans.controller.document.DocumentController;
import com.synloans.loans.mapper.document.DocumentMapper;
import com.synloans.loans.model.dto.document.DocumentDto;
import com.synloans.loans.model.dto.document.DocumentMetadata;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.service.document.DocumentService;
import com.synloans.loans.service.user.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Контроллер документов", description = "Документооборот по сделкам")
@RestController
@RequestMapping(Api.V1 + Api.DOCUMENT)
@RequiredArgsConstructor
public class DocumentControllerImpl implements DocumentController {

    private final UserService userService;

    private final DocumentService documentService;

    private final DocumentMapper documentMapper;

    @Override
    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentMetadata uploadDocument(
            @RequestParam("file") MultipartFile file
    ) {
        return documentService.upload(file, userService.getCurrentUser());
    }

    @Override
    @GetMapping("/{documentId}")
    public ResponseEntity<Resource> readDocument(@PathVariable("documentId") UUID documentId) {
        User currentUser = userService.getCurrentUser();
        DocumentDto document = documentService.getDocument(documentId, currentUser);

        return documentMapper.convertToResource(document);
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public List<DocumentMetadata> getDocuments() {
        return documentService.getAllUserDocuments(userService.getCurrentUser());
    }

}
