package com.synloans.loans.controller.company;

import com.synloans.loans.model.dto.CompanyDto;

import java.util.List;

public interface CompanyController {
    CompanyDto getById(long id);

    List<CompanyDto> getCompanies();
}
