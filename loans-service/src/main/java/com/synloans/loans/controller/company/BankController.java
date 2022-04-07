package com.synloans.loans.controller.company;

import com.synloans.loans.model.dto.CompanyDto;
import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.service.company.BankService;
import com.synloans.loans.service.exception.notfound.BankNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/banks")
@RequiredArgsConstructor
@Slf4j
public class BankController {
    private final BankService bankService;

    private final Converter<Bank, CompanyDto> bankToCompanyConverter;

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompanyDto getBankById(@PathVariable("id") Long id){
        Bank bank = bankService.getById(id);
        if (bank == null){
            throw new BankNotFoundException("Банк с id=" + id + " не найден");
        }
        return bankToCompanyConverter.convert(bank);
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CompanyDto> getAllBanks(){
        return bankService.getAll().stream()
                .map(bankToCompanyConverter::convert)
                .collect(Collectors.toList());
    }
}
