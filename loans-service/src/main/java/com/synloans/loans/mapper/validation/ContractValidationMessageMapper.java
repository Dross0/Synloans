package com.synloans.loans.mapper.validation;

import com.synloans.loans.model.dto.validation.CompanyValidation;
import com.synloans.loans.model.dto.validation.ContractValidationMessage;
import com.synloans.loans.model.dto.validation.LoanRequestValidation;
import com.synloans.loans.model.dto.validation.SyndicateParticipantValidation;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.document.Contract;
import com.synloans.loans.model.entity.loan.LoanRequest;
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContractValidationMessageMapper {

    @Mapping(source = "contract.id", target = "contractId")
    @Mapping(source = "contract.type", target = "contractType")
    @Mapping(source = "contract.attachedAt", target = "contractDate")
    @Mapping(source = "contractText", target = "contractText")
    @Mapping(source = "contract.loanRequest", target = "loanRequest")
    ContractValidationMessage convert(Contract contract, String contractText);


    @Mapping(source = "sum", target = "sum")
    @Mapping(source = "term", target = "term")
    @Mapping(source = "rate", target = "rate")
    @Mapping(source = "createDate", target = "createDate")
    @Mapping(source = "company", target = "borrower")
    @Mapping(source = "syndicate.participants", target = "syndicateParticipants")
    LoanRequestValidation convert(LoanRequest loanRequest);


    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "shortName", target = "shortName")
    @Mapping(source = "inn", target = "inn")
    @Mapping(source = "kpp", target = "kpp")
    @Mapping(source = "legalAddress", target = "legalAddress")
    @Mapping(source = "actualAddress", target = "actualAddress")
    @Mapping(source = "ogrn", target = "ogrn")
    @Mapping(source = "okpo", target = "okpo")
    @Mapping(source = "okato", target = "okato")
    CompanyValidation convert(Company company);


    @Mapping(source = "loanSum", target = "sum")
    @Mapping(source = "approveBankAgent", target = "approveBankAgent")
    @Mapping(source = "bank.company", target = "bank")
    SyndicateParticipantValidation convert(SyndicateParticipant syndicateParticipant);

}
