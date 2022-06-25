package com.synloans.loans.controller.company.impl;

import com.synloans.loans.controller.company.CompanyController;
import com.synloans.loans.mapper.Mapper;
import com.synloans.loans.model.dto.CompanyDto;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.service.company.CompanyService;
import com.synloans.loans.service.exception.advice.response.ErrorResponse;
import com.synloans.loans.service.exception.notfound.CompanyNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Контроллер компаний", description = "Служит для получения информации о компаниях")
@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyControllerImpl implements CompanyController {

    private final CompanyService companyService;

    private final Mapper<Company, CompanyDto> companyMapper;

    @Operation(summary = "Получение компании по id")
    @ApiResponse(
            responseCode = "200",
            description = "Успешное получение компании по id",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CompanyDto.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Компания с таким id не найден",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Override
    public CompanyDto getById(
            @Parameter(name = "id компании для поиска")
            @PathVariable("id") long id
    ){
        Company company = companyService.getById(id)
                .orElseThrow(() ->
                        new CompanyNotFoundException("Компания с id=" + id + " не найдена")
                );
        return companyMapper.mapFrom(company);
    }


    @Operation(summary = "Получение всех компаний")
    @ApiResponse(
            responseCode = "200",
            description = "Успешное получение всех компаний",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = CompanyDto.class))
            )
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Override
    public List<CompanyDto> getCompanies(){
        return companyService.getAll()
                .stream()
                .map(companyMapper::mapFrom)
                .collect(Collectors.toList());
    }
}
