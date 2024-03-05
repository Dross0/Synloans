package com.synloans.loans.document.extractor.text.impl;

import com.synloans.loans.document.extractor.text.TextExtractor;
import com.synloans.loans.service.exception.document.DocumentTextExtractionException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;

public class PdfTextExtractor implements TextExtractor {
    @Override
    public String extract(byte[] content) {
        try (PDDocument document = PDDocument.load(content)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new DocumentTextExtractionException(e);
        }
    }
}
