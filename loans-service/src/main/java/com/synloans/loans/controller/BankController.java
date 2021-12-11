package com.synloans.loans.controller;

import com.synloans.loans.model.dto.CompanyDto;
import com.synloans.loans.model.entity.Bank;
import com.synloans.loans.model.mapper.BankMapper;
import com.synloans.loans.service.company.BankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/banks")
@RequiredArgsConstructor
@Slf4j
public class BankController {
    private final BankService bankService;

    private final BankMapper bankMapper = new BankMapper();

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompanyDto getBankById(@PathVariable("id") Long id){
        Bank bank = bankService.getById(id);
        if (bank == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Банк с id=" + id + " не найден");
        }
        return bankMapper.bankToDto(bank);
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CompanyDto> getAllBanks(){
        return bankService.getAll().stream()
                .map(bankMapper::bankToDto)
                .collect(Collectors.toList());
    }
}
