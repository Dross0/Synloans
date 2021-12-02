package com.sinloans.loans.controller;

import com.sinloans.loans.model.entity.Company;
import com.sinloans.loans.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("company")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping("/{id}")
    @ResponseBody
    public Company getById(@PathVariable("id") Long id){
        return companyService.getById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Company with id=" + id +" not found")
                );
    }

    @GetMapping("/all")
    @ResponseBody
    public List<Company> getCompanies(){
        return new ArrayList<>(companyService.getAll());
    }


    @PostMapping("/add")
    public Company addCompany(@RequestBody Company company){
        return companyService.create(company);
    }
}
