package com.synloans.loans.mapper.document.contract;

import com.synloans.loans.model.dto.document.contract.ContractDto;
import com.synloans.loans.model.entity.document.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContractMapper {

    @Mapping(source = "id", target = "contractId")
    @Mapping(source = "loanRequest.id", target = "loanRequestId")
    @Mapping(source = "document.id", target = "documentId")
    ContractDto convert(Contract contract);

}
