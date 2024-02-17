package com.synloans.loans.mapper.document;


import com.synloans.loans.model.dto.document.DocumentDto;
import com.synloans.loans.model.entity.document.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Mapper(componentModel = "spring")
public abstract class DocumentMapper {

    @Mapping(source = ".", target = "metadata")
    public abstract DocumentDto convert(Document document);

    public ResponseEntity<Resource> convertToResource(Document document) {
        return convertToResource(document.getName(), document.getBody());
    }
    public ResponseEntity<Resource> convertToResource(DocumentDto document) {
        return convertToResource(document.getMetadata().getFilename(), document.getBody());
    }

    private ResponseEntity<Resource> convertToResource(String filename, byte[] body) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        ByteArrayResource resource = new ByteArrayResource(body);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(body.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}
