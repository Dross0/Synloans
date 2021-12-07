package com.sinloans.loans.controller;

import com.sinloans.loans.model.entity.Company;
import com.sinloans.loans.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Company getById(@PathVariable("id") Long id){
        return companyService.getById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Компания с id=" + id +" не найдена")
                );
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Company> getCompanies(){
        return new ArrayList<>(companyService.getAll());
    }


    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Company addCompany(@RequestBody Company company){
        return companyService.create(company);
    }
}
