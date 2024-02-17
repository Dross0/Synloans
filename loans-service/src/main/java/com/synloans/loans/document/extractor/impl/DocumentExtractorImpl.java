package com.synloans.loans.document.extractor.impl;

import com.synloans.loans.document.extractor.DocumentExtractor;
import com.synloans.loans.document.extractor.text.TextExtractor;
import com.synloans.loans.document.extractor.text.factory.TextExtractorFactory;
import com.synloans.loans.mapper.document.DocumentMetadataMapper;
import com.synloans.loans.model.entity.document.Document;
import com.synloans.loans.service.exception.document.DocumentTextExtractionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DocumentExtractorImpl implements DocumentExtractor {
    private static final String EXTRACTOR_NOT_FOUND = "Cant find text extractor for document with id=%s and name=%s";

    private final TextExtractorFactory textExtractorFactory;

    private final DocumentMetadataMapper metadataMapper;

    @Override
    public String getText(Document document) {
        log.debug("Start text extraction from document with id={} and name={}", document.getId(), document.getName());

        TextExtractor textExtractor = textExtractorFactory.getExtractor(metadataMapper.convert(document))
                .orElseThrow(() -> new DocumentTextExtractionException(String.format(EXTRACTOR_NOT_FOUND, document.getId(), document.getName())));

        String textContent = textExtractor.extract(document.getBody());
        log.debug("Successfully extracted text with len={} from document with id={} and name={}", textContent.length(), document.getId(), document.getName());
        return textContent;
    }
}
