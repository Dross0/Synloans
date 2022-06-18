package com.synloans.loans.controller.company;

import com.synloans.loans.model.dto.CompanyDto;
import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.service.company.BankService;
import com.synloans.loans.service.exception.advice.response.ErrorResponse;
import com.synloans.loans.service.exception.notfound.BankNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Контроллер банков", description = "Служит для получения информации о банках")
@RestController
@RequestMapping("/banks")
@RequiredArgsConstructor
@Slf4j
public class BankController {
    private final BankService bankService;

    private final Converter<Bank, CompanyDto> bankToCompanyConverter;

    @Operation(summary = "Получение банка по id")
    @ApiResponse(
            responseCode = "200",
            description = "Успешное получение банка по id",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CompanyDto.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Банк с таким id не найден",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompanyDto getBankById(
            @Parameter(name = "id банка для поиска")
            @PathVariable("id") Long id
    ){
        Bank bank = bankService.getById(id); //TODO Optional
        if (bank == null){
            throw new BankNotFoundException("Банк с id=" + id + " не найден");
        }
        return bankToCompanyConverter.convert(bank);
    }


    @Operation(summary = "Получение всех банков")
    @ApiResponse(
            responseCode = "200",
            description = "Успешное получение всех банков",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = CompanyDto.class))
            )
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CompanyDto> getAllBanks(){
        return bankService.getAll().stream()
                .map(bankToCompanyConverter::convert)
                .collect(Collectors.toList());
    }
}
