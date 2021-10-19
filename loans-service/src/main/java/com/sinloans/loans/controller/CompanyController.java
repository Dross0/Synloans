package com.sinloans.loans.controller;

import com.sinloans.loans.model.Company;
import com.sinloans.loans.repositories.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@RestController
@RequestMapping("company")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyRepository companyRepository;

    @GetMapping("/{id}")
    public Company getById(@PathVariable("id") Integer id){
        return companyRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Company with id=" + id +" not found")
                );
    }

    @GetMapping
    public List<Company> getCompanies(
            @RequestParam int count,
            @RequestParam(defaultValue = "0") int page){
        return companyRepository.findAll(PageRequest.of(page, count)).toList();
    }

    @GetMapping("/all")
    public List<Company> getCompanies(){
        return companyRepository.findAll();
    }

    @PostMapping("/add")
    public Company addCompany(@RequestBody Company company){
        return companyRepository.save(company);
    }
}
