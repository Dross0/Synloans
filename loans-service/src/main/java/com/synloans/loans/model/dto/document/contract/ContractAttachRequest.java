package com.synloans.loans.model.dto.document.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.synloans.loans.model.entity.document.ContractType;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@EqualsAndHashCode
public class ContractAttachRequest {

    @NotNull(message = "Id документа не может быть null")
    private final UUID documentId;

    @NotNull(message = "Тип контракта не может быть null")
    private final ContractType type;

    public ContractAttachRequest(
            @JsonProperty(value = "documentId", required = true) UUID documentId,
            @JsonProperty(value = "type", required = true) ContractType type
    ) {
        this.documentId = documentId;
        this.type = type;
    }
}
