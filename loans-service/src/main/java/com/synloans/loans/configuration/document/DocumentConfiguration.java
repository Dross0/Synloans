package com.synloans.loans.configuration.document;

import com.synloans.loans.document.extractor.text.TextExtractor;
import com.synloans.loans.document.extractor.text.factory.TextExtractorFactory;
import com.synloans.loans.document.extractor.text.factory.impl.ExtensionTextExtractorFactory;
import com.synloans.loans.document.extractor.text.impl.PdfTextExtractor;
import com.synloans.loans.document.extractor.text.impl.SimpleTextExtractor;
import com.synloans.loans.document.extractor.text.impl.WordTextExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class DocumentConfiguration {

    @Bean
    public TextExtractorFactory textExtractorFactory() {
        TextExtractor simpleExtractor = new SimpleTextExtractor();
        TextExtractor pdfExtractor = new PdfTextExtractor();
        TextExtractor wordExtractor = new WordTextExtractor();
        return new ExtensionTextExtractorFactory(
                Map.of(
                        "docx", wordExtractor,
                        "pdf", pdfExtractor,
                        "txt", simpleExtractor,
                        "", simpleExtractor
                )
        );
    }

}
