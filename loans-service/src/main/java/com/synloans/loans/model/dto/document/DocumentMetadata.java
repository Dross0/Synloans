package com.synloans.loans.model.dto.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class DocumentMetadata {

    private UUID documentId;

    private String filename;

    private Instant createdAt;

    private Instant updatedAt;

}
