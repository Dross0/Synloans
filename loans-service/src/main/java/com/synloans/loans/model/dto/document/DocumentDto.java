package com.synloans.loans.model.dto.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DocumentDto {

    private DocumentMetadata metadata;

    private byte[] body;

}
