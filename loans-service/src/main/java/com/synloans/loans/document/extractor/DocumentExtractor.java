package com.synloans.loans.document.extractor;

import com.synloans.loans.model.entity.document.Document;

public interface DocumentExtractor {

    String getText(Document document);

}
