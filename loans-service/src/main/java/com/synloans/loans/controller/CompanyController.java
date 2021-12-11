package com.synloans.loans.controller;

import com.synloans.loans.model.dto.CompanyDto;
import com.synloans.loans.model.entity.Company;
import com.synloans.loans.model.mapper.CompanyMapper;
import com.synloans.loans.service.company.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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
    public List<CompanyDto> getCompanies(){
        return companyService.getAll()
                .stream()
                .map(companyMapper::entityToDto)
                .collect(Collectors.toList());
    }
}
