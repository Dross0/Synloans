package com.synloans.loans.document.extractor.text.factory;

import com.synloans.loans.document.extractor.text.TextExtractor;
import com.synloans.loans.model.dto.document.DocumentMetadata;

import java.util.Optional;

public interface TextExtractorFactory {

    Optional<TextExtractor> getExtractor(DocumentMetadata documentMetadata);

}
