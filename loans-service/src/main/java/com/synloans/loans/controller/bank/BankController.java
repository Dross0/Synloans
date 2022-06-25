package com.synloans.loans.controller.bank;

import com.synloans.loans.model.dto.CompanyDto;

import java.util.List;

public interface BankController {
    CompanyDto getBankById(long id);

    List<CompanyDto> getAllBanks();
}
