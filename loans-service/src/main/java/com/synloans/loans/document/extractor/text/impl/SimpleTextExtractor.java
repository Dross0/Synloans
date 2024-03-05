package com.synloans.loans.document.extractor.text.impl;

import com.synloans.loans.document.extractor.text.TextExtractor;

import java.nio.charset.StandardCharsets;

public class SimpleTextExtractor implements TextExtractor {

    @Override
    public String extract(byte[] content) {
        return new String(content, StandardCharsets.UTF_8);
    }

}
