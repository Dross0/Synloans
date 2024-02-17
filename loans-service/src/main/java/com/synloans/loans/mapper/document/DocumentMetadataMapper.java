package com.synloans.loans.mapper.document;

import com.synloans.loans.model.dto.document.DocumentMetadata;
import com.synloans.loans.model.entity.document.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMetadataMapper {

    @Mapping(source = "id", target = "documentId")
    @Mapping(source = "name", target = "filename")
    @Mapping(source = "createDate", target = "createdAt")
    @Mapping(source = "lastUpdate", target = "updatedAt")
    DocumentMetadata convert(Document document);

}
