package com.sinloans.loans.controller;

import com.sinloans.loans.model.Company;
import com.sinloans.loans.model.LoanRequest;
import com.sinloans.loans.repositories.CompanyRepository;
import com.sinloans.loans.service.LoanRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loan/request")
@RequiredArgsConstructor
public class LoanRequestController {
    private final LoanRequestService requestService;
    private final CompanyRepository companyRepository;


    @GetMapping("/list")
    public List<LoanRequest> loanRequestList(@RequestParam(value = "limit", required = false) Long limit){
        return requestService.getListOfRequests(limit);
    }

    @GetMapping("/{id}")
    public LoanRequest loanRequest(@PathVariable("id") Long id){
        return requestService.getById(id);
    }

    @GetMapping("/")
    public Company company(){
        Company company = new Company();
        company.setFullName("SberBank");
        company.setShortName("Sber");
        company.setOkato("10302");
        company.setLegalAddress("Moscow");
        company.setTin("1234567890");
        company.setIec("22");
        company.setPsrn("21");
        company.setActualAddress("a");
        company.setOkpo("5555");
        companyRepository.save(company);
        return company;
    }
}
