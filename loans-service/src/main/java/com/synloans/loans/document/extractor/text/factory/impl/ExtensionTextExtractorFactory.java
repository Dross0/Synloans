package com.synloans.loans.document.extractor.text.factory.impl;

import com.synloans.loans.document.extractor.text.TextExtractor;
import com.synloans.loans.document.extractor.text.factory.TextExtractorFactory;
import com.synloans.loans.model.dto.document.DocumentMetadata;
import org.apache.commons.io.FilenameUtils;

import java.util.Map;
import java.util.Optional;

public class ExtensionTextExtractorFactory implements TextExtractorFactory {

    private final Map<String, TextExtractor> extensionExtractors;

    public ExtensionTextExtractorFactory(Map<String, TextExtractor> extensionExtractors) {
        this.extensionExtractors = extensionExtractors;
    }

    @Override
    public Optional<TextExtractor> getExtractor(DocumentMetadata documentMetadata) {
        String extension = FilenameUtils.getExtension(documentMetadata.getFilename());
        return Optional.ofNullable(extensionExtractors.get(extension));
    }
}
