package com.sinloans.loans.controller;

import com.sinloans.loans.model.dto.CompanyDto;
import com.sinloans.loans.model.entity.Company;
import com.sinloans.loans.model.mapper.CompanyMapper;
import com.sinloans.loans.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyMapper companyMapper = new CompanyMapper();

    private final CompanyService companyService;

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CompanyDto getById(@PathVariable("id") Long id){
        Company company = companyService.getById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Компания с id=" + id + " не найдена")
                );
        return companyMapper.entityToDto(company);
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Collection<CompanyDto> getCompanies(){
        return companyService.getAll()
                .stream()
                .map(companyMapper::entityToDto)
                .collect(Collectors.toList());
    }
}
