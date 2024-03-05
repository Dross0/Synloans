package com.synloans.loans.controller.company.impl;

import com.synloans.loans.controller.BaseControllerTest;
import com.synloans.loans.controller.company.CompanyController;
import com.synloans.loans.mapper.CompanyMapper;
import com.synloans.loans.model.dto.CompanyDto;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.service.company.CompanyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import utils.NoConvertersFilter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        value = CompanyController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = NoConvertersFilter.class)
)
class CompanyControllerImplTest extends BaseControllerTest {

    public static final String BASE_PATH = "/companies";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CompanyService companyService;

    @MockBean
    CompanyMapper companyMapper;


    @Test
    @DisplayName("Получение компании по id")
    @WithMockUser
    void getCompanyByIdTest() throws Exception {
        long companyId = 12L;

        Company company = new Company();

        CompanyDto companyDto = CompanyDto.builder()
                .id(companyId)
                .fullName("cFn")
                .shortName("cSn")
                .actualAddress("cAa")
                .legalAddress("cLa")
                .inn("1234567890")
                .kpp("123456789")
                .build();

        when(companyService.getById(companyId)).thenReturn(Optional.of(company));
        when(companyMapper.mapFrom(company)).thenReturn(companyDto);

        mockMvc.perform(get(BASE_PATH + "/{id}", companyId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.id").value(companyId),
                        jsonPath("$.fullName").value(companyDto.getFullName()),
                        jsonPath("$.shortName").value(companyDto.getShortName()),
                        jsonPath("$.actualAddress").value(companyDto.getActualAddress()),
                        jsonPath("$.legalAddress").value(companyDto.getLegalAddress()),
                        jsonPath("$.inn").value(companyDto.getInn()),
                        jsonPath("$.kpp").value(companyDto.getKpp())
                );

        verify(companyService, times(1)).getById(companyId);
        verify(companyMapper, times(1)).mapFrom(company);
    }

    @Test
    @DisplayName("Получение несуществующей компании по id")
    @WithMockUser
    void getCompanyByIdNotFoundTest() throws Exception {
        long companyId = 12L;

        when(companyService.getById(companyId)).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_PATH + "/{id}", companyId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.code").value(404),
                        jsonPath("$.status").value("NOT_FOUND"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").exists(),
                        jsonPath("$.timestamp").exists()
                );

        verify(companyService, times(1)).getById(companyId);
    }

    @Test
    @DisplayName("Получение компании по id без авторизации")
    void getCompanyByIdAccessDeniedTest() throws Exception {
        long companyId = 12L;

        mockMvc.perform(get(BASE_PATH + "/{id}", companyId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(blankOrNullString()));

        verify(companyService, never()).getById(companyId);
    }

    @Test
    @DisplayName("Получение всех компаний")
    @WithMockUser
    void getCompaniesTest() throws Exception {
        Company company1 = new Company();
        Company company2 = new Company();

        CompanyDto companyDto1 = CompanyDto.builder()
                .id(12L)
                .fullName("cFn1")
                .shortName("cSn1")
                .actualAddress("cAa1")
                .legalAddress("cLa1")
                .inn("1111111110")
                .kpp("111101111")
                .build();
        CompanyDto companyDto2 = CompanyDto.builder()
                .id(122L)
                .fullName("cFn2")
                .shortName("cSn2")
                .actualAddress("cAa2")
                .legalAddress("cLa2")
                .inn("2222222220")
                .kpp("222202222")
                .build();

        when(companyService.getAll()).thenReturn(List.of(company1, company2));
        when(companyMapper.mapFrom(company1)).thenReturn(companyDto1);
        when(companyMapper.mapFrom(company2)).thenReturn(companyDto2);

        mockMvc.perform(get(BASE_PATH))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$", hasSize(2)),
                        jsonPath("$[0].id").value(companyDto1.getId()),
                        jsonPath("$[0].fullName").value(companyDto1.getFullName()),
                        jsonPath("$[0].shortName").value(companyDto1.getShortName()),
                        jsonPath("$[0].actualAddress").value(companyDto1.getActualAddress()),
                        jsonPath("$[0].legalAddress").value(companyDto1.getLegalAddress()),
                        jsonPath("$[0].inn").value(companyDto1.getInn()),
                        jsonPath("$[0].kpp").value(companyDto1.getKpp()),
                        jsonPath("$[1].id").value(companyDto2.getId()),
                        jsonPath("$[1].fullName").value(companyDto2.getFullName()),
                        jsonPath("$[1].shortName").value(companyDto2.getShortName()),
                        jsonPath("$[1].actualAddress").value(companyDto2.getActualAddress()),
                        jsonPath("$[1].legalAddress").value(companyDto2.getLegalAddress()),
                        jsonPath("$[1].inn").value(companyDto2.getInn()),
                        jsonPath("$[1].kpp").value(companyDto2.getKpp())
                );

        verify(companyService, times(1)).getAll();
        verify(companyMapper, times(1)).mapFrom(company1);
        verify(companyMapper, times(1)).mapFrom(company2);
    }

    @Test
    @DisplayName("Получение пустого списка компаний")
    @WithMockUser
    void getCompaniesEmptyListTest() throws Exception {

        when(companyService.getAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_PATH ))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(companyService, times(1)).getAll();
        verify(companyMapper, never()).mapFrom(any());
    }

    @Test
    @DisplayName("Получение всех компаний без авторизации")
    void getCompaniesAccessDeniedTest() throws Exception {

        mockMvc.perform(get(BASE_PATH))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(blankOrNullString()));

        verify(companyService, never()).getAll();
    }



}