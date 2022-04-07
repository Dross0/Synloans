package com.synloans.loans.controller.company;

import com.synloans.loans.mapper.Mapper;
import com.synloans.loans.model.dto.CompanyDto;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.service.company.CompanyService;
import com.synloans.loans.service.exception.notfound.CompanyNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    private final Mapper<Company, CompanyDto> companyMapper;

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CompanyDto getById(@PathVariable("id") Long id){
        Company company = companyService.getById(id)
                .orElseThrow(() ->
                        new CompanyNotFoundException("Компания с id=" + id + " не найдена")
                );
        return companyMapper.mapFrom(company);
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<CompanyDto> getCompanies(){
        return companyService.getAll()
                .stream()
                .map(companyMapper::mapFrom)
                .collect(Collectors.toList());
    }
}
