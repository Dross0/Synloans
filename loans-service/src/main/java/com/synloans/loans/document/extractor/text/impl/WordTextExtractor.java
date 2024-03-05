package com.synloans.loans.document.extractor.text.impl;

import com.synloans.loans.document.extractor.text.TextExtractor;
import com.synloans.loans.service.exception.document.DocumentTextExtractionException;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class WordTextExtractor implements TextExtractor {
    @Override
    public String extract(byte[] content) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
            XWPFDocument document = new XWPFDocument(inputStream);
            XWPFWordExtractor wordExtractor = new XWPFWordExtractor(document);
            return wordExtractor.getText();
        } catch (IOException e) {
            throw new DocumentTextExtractionException(e);
        }
    }
}
