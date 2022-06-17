package com.synloans.loans.controller.loan;

import com.synloans.loans.controller.BaseControllerTest;
import com.synloans.loans.mapper.LoanRequestCollectionConverter;
import com.synloans.loans.mapper.converter.LoanRequestConverter;
import com.synloans.loans.mapper.converter.SyndicateParticipantConverter;
import com.synloans.loans.model.dto.BankParticipantInfo;
import com.synloans.loans.model.dto.CompanyDto;
import com.synloans.loans.model.dto.LoanSum;
import com.synloans.loans.model.dto.collection.LoanRequestCollection;
import com.synloans.loans.model.dto.collection.LoanRequestCollectionResponse;
import com.synloans.loans.model.dto.loanrequest.LoanRequestDto;
import com.synloans.loans.model.dto.loanrequest.LoanRequestInfo;
import com.synloans.loans.model.dto.loanrequest.LoanRequestResponse;
import com.synloans.loans.model.dto.loanrequest.LoanRequestStatus;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.loan.LoanRequest;
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant;
import com.synloans.loans.model.entity.user.Role;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.security.UserRole;
import com.synloans.loans.service.exception.ForbiddenResourceException;
import com.synloans.loans.service.exception.notfound.LoanRequestNotFoundException;
import com.synloans.loans.service.loan.LoanRequestService;
import com.synloans.loans.service.syndicate.SyndicateParticipantService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import utils.NoConvertersFilter;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.JsonHelper.asJsonString;


@WebMvcTest(
        value = LoanRequestController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = NoConvertersFilter.class)
)
class LoanRequestControllerTest extends BaseControllerTest {

    private static final String BASE_PATH = "/loans/requests";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    LoanRequestService loanRequestService;

    @MockBean
    SyndicateParticipantService syndicateParticipantService;

    @MockBean
    LoanRequestConverter loanRequestConverter;

    @MockBean
    LoanRequestCollectionConverter loanRequestCollectionConverter;

    @MockBean
    SyndicateParticipantConverter syndicateParticipantConverter;

    @Captor
    ArgumentCaptor<Authentication> authenticationArgumentCaptor;

    @Test
    @DisplayName("Создание заявки на кредит без авторизации пользователя")
    void createLoanRequestWithAccessDeniedTest() throws Exception {
        LoanRequestDto loanRequestDto = new LoanRequestDto(
                120_232L,
                0.12,
                13
        );

        mockMvc.perform(
                post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, loanRequestDto))
        )
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(blankOrNullString()));

        verify(userService, never()).getCurrentUser(any());
        verify(loanRequestService, never()).createRequest(any(), any());
        verify(loanRequestConverter, never()).convert(any());
    }

    @ParameterizedTest
    @MethodSource("notValidLoanRequest")
    @DisplayName("Создание заявки на кредит с невалидными данными")
    @WithMockUser
    void createLoanRequestWithInvalidDataTest(LoanRequestDto loanRequestDto) throws Exception {
        mockMvc.perform(
                post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, loanRequestDto))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.code").value(400),
                        jsonPath("$.status").value("BAD_REQUEST"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(userService, never()).getCurrentUser(any());
        verify(loanRequestService, never()).createRequest(any(), any());
        verify(loanRequestConverter, never()).convert(any());
    }

    private static Stream<Arguments> notValidLoanRequest() {
        return Stream.of(
                Arguments.of(
                        new LoanRequestDto(
                                -12,
                                0.2,
                                2
                        )
                ),
                Arguments.of(
                        new LoanRequestDto(
                                123,
                                -0.2,
                                2
                        )
                ),
                Arguments.of(
                        new LoanRequestDto(
                                12,
                                0.2,
                                -2
                        )
                )
        );
    }

    @Test
    @DisplayName("Создание заявки на кредит")
    @WithMockUser
    void createLoanRequestTest() throws Exception {

        LoanRequestDto loanRequestDto = new LoanRequestDto(
                123_230,
                0.2,
                3
        );

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        CompanyDto companyDto = CompanyDto.builder()
                .id(10L)
                .fullName("comFullName")
                .shortName("comShortName")
                .actualAddress("comActAddress")
                .legalAddress("comLegAddress")
                .inn("1234567890")
                .kpp("111111111")
                .build();

        LoanRequest loanRequest = new LoanRequest();

        LoanRequestInfo loanRequestInfo = LoanRequestInfo.builder()
                .id(133L)
                .dateCreate(LocalDate.of(2022, Month.JUNE, 17))
                .maxRate(0.2)
                .sum(LoanSum.valueOf(123_230))
                .status(LoanRequestStatus.OPEN)
                .term(3)
                .build();


        LoanRequestResponse loanRequestResponse = LoanRequestResponse.builder()
                .borrower(companyDto)
                .banks(Collections.emptyList())
                .info(loanRequestInfo)
                .build();


        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanRequestService.createRequest(loanRequestDto, company)).thenReturn(loanRequest);
        when(loanRequestConverter.convert(loanRequest)).thenReturn(loanRequestResponse);

        mockMvc.perform(
                post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, loanRequestDto))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.banks", hasSize(0)),
                        jsonPath("$.borrower.id").value(companyDto.getId()),
                        jsonPath("$.borrower.fullName").value(companyDto.getFullName()),
                        jsonPath("$.borrower.shortName").value(companyDto.getShortName()),
                        jsonPath("$.borrower.actualAddress").value(companyDto.getActualAddress()),
                        jsonPath("$.borrower.legalAddress").value(companyDto.getLegalAddress()),
                        jsonPath("$.borrower.kpp").value(companyDto.getKpp()),
                        jsonPath("$.borrower.inn").value(companyDto.getInn()),
                        jsonPath("$.info.id").value(loanRequestInfo.getId()),
                        jsonPath("$.info.status").value(loanRequestInfo.getStatus().name()),
                        jsonPath("$.info.term").value(loanRequestInfo.getTerm()),
                        jsonPath("$.info.maxRate").value(loanRequestInfo.getMaxRate()),
                        jsonPath("$.info.dateCreate").value("2022-06-17"),
                        jsonPath("$.info.sum.value").value(loanRequestInfo.getSum().getValue()),
                        jsonPath("$.info.sum.unit").value(loanRequestInfo.getSum().getUnit().name()),
                        jsonPath("$.info.dateIssue").hasJsonPath()
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(loanRequestService, times(1)).createRequest(loanRequestDto, company);
        verify(loanRequestConverter, times(1)).convert(loanRequest);
    }

    @Test
    @DisplayName("Получение компанией своих заявок без авторизации пользователя")
    void getCompanyRequestsWithAccessDeniedTest() throws Exception {

        mockMvc.perform(get(BASE_PATH))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(blankOrNullString()));

        verify(userService, never()).getCurrentUser(any());
        verify(loanRequestConverter, never()).convert(any());
    }

    @Test
    @DisplayName("Получение компанией своих заявок, когда нет заявок")
    @WithMockUser
    void getCompanyRequestsWithEmptyListTest() throws Exception {
        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        company.setLoanRequests(Collections.emptySet());

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);

        mockMvc.perform(get(BASE_PATH))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(loanRequestConverter, never()).convert(any());
    }

    @Test
    @DisplayName("Получение компанией своих заявок")
    @WithMockUser
    void getCompanyRequests() throws Exception {
        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        CompanyDto companyDto1 = CompanyDto.builder()
                .id(10L)
                .fullName("comFullName1")
                .shortName("comShortName1")
                .actualAddress("comActAddress1")
                .legalAddress("comLegAddress1")
                .inn("1234567890")
                .kpp("111111111")
                .build();

        LoanRequestInfo loanRequestInfo1 = LoanRequestInfo.builder()
                .id(133L)
                .dateCreate(LocalDate.of(2022, Month.JUNE, 17))
                .maxRate(0.2)
                .sum(LoanSum.valueOf(123_230))
                .status(LoanRequestStatus.OPEN)
                .term(3)
                .build();

        CompanyDto companyDto2 = CompanyDto.builder()
                .id(123L)
                .fullName("comFullName2")
                .shortName("comShortName2")
                .actualAddress("comActAddress2")
                .legalAddress("comLegAddress2")
                .inn("3232424")
                .kpp("155453")
                .build();

        LoanRequestInfo loanRequestInfo2 = LoanRequestInfo.builder()
                .id(323L)
                .dateCreate(LocalDate.of(2021, Month.MAY, 12))
                .maxRate(0.4)
                .sum(LoanSum.valueOf(3_323_000))
                .status(LoanRequestStatus.CLOSE)
                .term(12)
                .dateIssue(LocalDate.of(2021, Month.AUGUST, 4))
                .build();

        BankParticipantInfo bank1 = BankParticipantInfo.builder()
                .approveBankAgent(true)
                .id(1666L)
                .name("Sber")
                .sum(LoanSum.valueOf(3_000_000))
                .issuedSum(3_000_000)
                .build();
        BankParticipantInfo bank2 = BankParticipantInfo.builder()
                .approveBankAgent(false)
                .id(1622L)
                .name("Vtb")
                .sum(LoanSum.valueOf(200_000))
                .issuedSum(100_000)
                .build();


        LoanRequestResponse loanRequestResponse1 = LoanRequestResponse.builder()
                .borrower(companyDto1)
                .banks(Collections.emptyList())
                .info(loanRequestInfo1)
                .build();
        LoanRequestResponse loanRequestResponse2 = LoanRequestResponse.builder()
                .borrower(companyDto2)
                .banks(List.of(bank1, bank2))
                .info(loanRequestInfo2)
                .build();

        LoanRequest loanRequest1 = new LoanRequest();
        LoanRequest loanRequest2 = new LoanRequest();

        Set<LoanRequest> loanRequests = new LinkedHashSet<>();
        loanRequests.add(loanRequest1);
        loanRequests.add(loanRequest2);

        company.setLoanRequests(loanRequests);

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanRequestConverter.convert(loanRequest1)).thenReturn(loanRequestResponse1);
        when(loanRequestConverter.convert(loanRequest2)).thenReturn(loanRequestResponse2);

        mockMvc.perform(get(BASE_PATH))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$", hasSize(2)),
                        jsonPath("$[0].banks", hasSize(0)),
                        jsonPath("$[0].borrower.id").value(companyDto1.getId()),
                        jsonPath("$[0].borrower.fullName").value(companyDto1.getFullName()),
                        jsonPath("$[0].borrower.shortName").value(companyDto1.getShortName()),
                        jsonPath("$[0].borrower.actualAddress").value(companyDto1.getActualAddress()),
                        jsonPath("$[0].borrower.legalAddress").value(companyDto1.getLegalAddress()),
                        jsonPath("$[0].borrower.kpp").value(companyDto1.getKpp()),
                        jsonPath("$[0].borrower.inn").value(companyDto1.getInn()),
                        jsonPath("$[0].info.id").value(loanRequestInfo1.getId()),
                        jsonPath("$[0].info.status").value(loanRequestInfo1.getStatus().name()),
                        jsonPath("$[0].info.term").value(loanRequestInfo1.getTerm()),
                        jsonPath("$[0].info.maxRate").value(loanRequestInfo1.getMaxRate()),
                        jsonPath("$[0].info.dateCreate").value("2022-06-17"),
                        jsonPath("$[0].info.sum.value").value(loanRequestInfo1.getSum().getValue()),
                        jsonPath("$[0].info.sum.unit").value(loanRequestInfo1.getSum().getUnit().name()),
                        jsonPath("$[0].info.dateIssue").hasJsonPath(),
                        jsonPath("$[1].borrower.id").value(companyDto2.getId()),
                        jsonPath("$[1].borrower.fullName").value(companyDto2.getFullName()),
                        jsonPath("$[1].borrower.shortName").value(companyDto2.getShortName()),
                        jsonPath("$[1].borrower.actualAddress").value(companyDto2.getActualAddress()),
                        jsonPath("$[1].borrower.legalAddress").value(companyDto2.getLegalAddress()),
                        jsonPath("$[1].borrower.kpp").value(companyDto2.getKpp()),
                        jsonPath("$[1].borrower.inn").value(companyDto2.getInn()),
                        jsonPath("$[1].info.id").value(loanRequestInfo2.getId()),
                        jsonPath("$[1].info.status").value(loanRequestInfo2.getStatus().name()),
                        jsonPath("$[1].info.term").value(loanRequestInfo2.getTerm()),
                        jsonPath("$[1].info.maxRate").value(loanRequestInfo2.getMaxRate()),
                        jsonPath("$[1].info.dateCreate").value("2021-05-12"),
                        jsonPath("$[1].info.sum.value").value(loanRequestInfo2.getSum().getValue()),
                        jsonPath("$[1].info.sum.unit").value(loanRequestInfo2.getSum().getUnit().name()),
                        jsonPath("$[1].info.dateIssue").value("2021-08-04"),
                        jsonPath("$[1].banks", hasSize(2)),
                        jsonPath("$[1].banks[0].approveBankAgent").value(bank1.isApproveBankAgent()),
                        jsonPath("$[1].banks[0].id").value(bank1.getId()),
                        jsonPath("$[1].banks[0].name").value(bank1.getName()),
                        jsonPath("$[1].banks[0].sum.value").value(bank1.getSum().getValue()),
                        jsonPath("$[1].banks[0].sum.unit").value(bank1.getSum().getUnit().name()),
                        jsonPath("$[1].banks[0].issuedSum").value(bank1.getIssuedSum()),
                        jsonPath("$[1].banks[1].approveBankAgent").value(bank2.isApproveBankAgent()),
                        jsonPath("$[1].banks[1].id").value(bank2.getId()),
                        jsonPath("$[1].banks[1].name").value(bank2.getName()),
                        jsonPath("$[1].banks[1].sum.value").value(bank2.getSum().getValue()),
                        jsonPath("$[1].banks[1].sum.unit").value(bank2.getSum().getUnit().name()),
                        jsonPath("$[1].banks[1].issuedSum").value(bank2.getIssuedSum())
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(loanRequestConverter, times(1)).convert(loanRequest1);
        verify(loanRequestConverter, times(1)).convert(loanRequest2);
    }

    @Test
    @DisplayName("Получение заявки по id без авторизации пользователя")
    void getRequestByIdWithAccessDeniedTest() throws Exception {
        long loanRequestId = 12L;

        mockMvc.perform(get(BASE_PATH + "/{id}", loanRequestId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(blankOrNullString()));

        verify(userService, never()).getCurrentUser(any());
        verify(loanRequestService, never()).getById(anyLong());
        verify(loanRequestConverter, never()).convert(any());
    }

    @Test
    @DisplayName("Получение заявки по id, когда пользователь не банк и заявка не его")
    @WithMockUser(roles = "COMPANY")
    void getRequestByIdWithDifferentOwnerTest() throws Exception {
        long requestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);
        Role role = new Role();
        role.setName(UserRole.ROLE_COMPANY);
        user.setRoles(Set.of(role));

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanRequestService.getOwnedCompanyLoanRequestById(requestId, company))
                .thenThrow(ForbiddenResourceException.class);

        mockMvc.perform(get(BASE_PATH + "/{id}", requestId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.code").value(403),
                        jsonPath("$.status").value("FORBIDDEN"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(userService, times(2)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(loanRequestService, times(1)).getOwnedCompanyLoanRequestById(requestId, company);

        verify(loanRequestConverter, never()).convert(any());
    }

    @Test
    @DisplayName("Получение заявки по id, когда нет заявки")
    @WithMockUser(roles = "COMPANY")
    void getRequestByIdNotFoundTest() throws Exception {
        long requestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);
        Role role = new Role();
        role.setName(UserRole.ROLE_COMPANY);
        user.setRoles(Set.of(role));

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanRequestService.getOwnedCompanyLoanRequestById(requestId, company))
                .thenThrow(LoanRequestNotFoundException.class);

        mockMvc.perform(get(BASE_PATH + "/{id}", requestId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.code").value(404),
                        jsonPath("$.status").value("NOT_FOUND"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(userService, times(2)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(loanRequestService, times(1)).getOwnedCompanyLoanRequestById(requestId, company);

        verify(loanRequestConverter, never()).convert(any());
    }

    @Test
    @DisplayName("Получение банком, заявки по id")
    @WithMockUser(roles = "BANK")
    void getRequestByIdByBankTest() throws Exception {
        long requestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);
        Role role = new Role();
        role.setName(UserRole.ROLE_BANK);
        user.setRoles(Set.of(role));

        CompanyDto companyDto = CompanyDto.builder()
                .id(123L)
                .fullName("comFullName2")
                .shortName("comShortName2")
                .actualAddress("comActAddress2")
                .legalAddress("comLegAddress2")
                .inn("3232424")
                .kpp("155453")
                .build();

        LoanRequestInfo loanRequestInfo = LoanRequestInfo.builder()
                .id(323L)
                .dateCreate(LocalDate.of(2021, Month.MAY, 12))
                .maxRate(0.4)
                .sum(LoanSum.valueOf(3_323_000))
                .status(LoanRequestStatus.CLOSE)
                .term(12)
                .dateIssue(LocalDate.of(2021, Month.AUGUST, 4))
                .build();

        BankParticipantInfo bank1 = BankParticipantInfo.builder()
                .approveBankAgent(true)
                .id(1666L)
                .name("Sber")
                .sum(LoanSum.valueOf(3_000_000))
                .issuedSum(3_000_000)
                .build();
        BankParticipantInfo bank2 = BankParticipantInfo.builder()
                .approveBankAgent(false)
                .id(1622L)
                .name("Vtb")
                .sum(LoanSum.valueOf(200_000))
                .issuedSum(100_000)
                .build();

        LoanRequestResponse loanRequestResponse = LoanRequestResponse.builder()
                .borrower(companyDto)
                .banks(List.of(bank1, bank2))
                .info(loanRequestInfo)
                .build();

        LoanRequest loanRequest = new LoanRequest();

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanRequestService.getById(requestId)).thenReturn(Optional.of(loanRequest));

        when(loanRequestConverter.convert(loanRequest)).thenReturn(loanRequestResponse);

        mockMvc.perform(get(BASE_PATH + "/{id}", requestId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.borrower.id").value(companyDto.getId()),
                        jsonPath("$.borrower.fullName").value(companyDto.getFullName()),
                        jsonPath("$.borrower.shortName").value(companyDto.getShortName()),
                        jsonPath("$.borrower.actualAddress").value(companyDto.getActualAddress()),
                        jsonPath("$.borrower.legalAddress").value(companyDto.getLegalAddress()),
                        jsonPath("$.borrower.kpp").value(companyDto.getKpp()),
                        jsonPath("$.borrower.inn").value(companyDto.getInn()),
                        jsonPath("$.info.id").value(loanRequestInfo.getId()),
                        jsonPath("$.info.status").value(loanRequestInfo.getStatus().name()),
                        jsonPath("$.info.term").value(loanRequestInfo.getTerm()),
                        jsonPath("$.info.maxRate").value(loanRequestInfo.getMaxRate()),
                        jsonPath("$.info.dateCreate").value("2021-05-12"),
                        jsonPath("$.info.sum.value").value(loanRequestInfo.getSum().getValue()),
                        jsonPath("$.info.sum.unit").value(loanRequestInfo.getSum().getUnit().name()),
                        jsonPath("$.info.dateIssue").value("2021-08-04"),
                        jsonPath("$.banks", hasSize(2)),
                        jsonPath("$.banks[0].approveBankAgent").value(bank1.isApproveBankAgent()),
                        jsonPath("$.banks[0].id").value(bank1.getId()),
                        jsonPath("$.banks[0].name").value(bank1.getName()),
                        jsonPath("$.banks[0].sum.value").value(bank1.getSum().getValue()),
                        jsonPath("$.banks[0].sum.unit").value(bank1.getSum().getUnit().name()),
                        jsonPath("$.banks[0].issuedSum").value(bank1.getIssuedSum()),
                        jsonPath("$.banks[1].approveBankAgent").value(bank2.isApproveBankAgent()),
                        jsonPath("$.banks[1].id").value(bank2.getId()),
                        jsonPath("$.banks[1].name").value(bank2.getName()),
                        jsonPath("$.banks[1].sum.value").value(bank2.getSum().getValue()),
                        jsonPath("$.banks[1].sum.unit").value(bank2.getSum().getUnit().name()),
                        jsonPath("$.banks[1].issuedSum").value(bank2.getIssuedSum())
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(loanRequestService, times(1)).getById(requestId);

        verify(loanRequestConverter, times(1)).convert(loanRequest);
    }

    @Test
    @DisplayName("Получение компанией своей заявки по id")
    @WithMockUser(roles = "COMPANY")
    void getRequestByIdByOwnerTest() throws Exception {
        long requestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);
        Role role = new Role();
        role.setName(UserRole.ROLE_COMPANY);
        user.setRoles(Set.of(role));

        CompanyDto companyDto = CompanyDto.builder()
                .id(123L)
                .fullName("comFullName2")
                .shortName("comShortName2")
                .actualAddress("comActAddress2")
                .legalAddress("comLegAddress2")
                .inn("3232424")
                .kpp("155453")
                .build();

        LoanRequestInfo loanRequestInfo = LoanRequestInfo.builder()
                .id(323L)
                .dateCreate(LocalDate.of(2021, Month.MAY, 12))
                .maxRate(0.4)
                .sum(LoanSum.valueOf(3_323_000))
                .status(LoanRequestStatus.CLOSE)
                .term(12)
                .dateIssue(LocalDate.of(2021, Month.AUGUST, 4))
                .build();

        BankParticipantInfo bank1 = BankParticipantInfo.builder()
                .approveBankAgent(true)
                .id(1666L)
                .name("Sber")
                .sum(LoanSum.valueOf(3_000_000))
                .issuedSum(3_000_000)
                .build();
        BankParticipantInfo bank2 = BankParticipantInfo.builder()
                .approveBankAgent(false)
                .id(1622L)
                .name("Vtb")
                .sum(LoanSum.valueOf(200_000))
                .issuedSum(100_000)
                .build();

        LoanRequestResponse loanRequestResponse = LoanRequestResponse.builder()
                .borrower(companyDto)
                .banks(List.of(bank1, bank2))
                .info(loanRequestInfo)
                .build();

        LoanRequest loanRequest = new LoanRequest();

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanRequestService.getOwnedCompanyLoanRequestById(requestId, company))
                .thenReturn(loanRequest);

        when(loanRequestConverter.convert(loanRequest)).thenReturn(loanRequestResponse);

        mockMvc.perform(get(BASE_PATH + "/{id}", requestId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.borrower.id").value(companyDto.getId()),
                        jsonPath("$.borrower.fullName").value(companyDto.getFullName()),
                        jsonPath("$.borrower.shortName").value(companyDto.getShortName()),
                        jsonPath("$.borrower.actualAddress").value(companyDto.getActualAddress()),
                        jsonPath("$.borrower.legalAddress").value(companyDto.getLegalAddress()),
                        jsonPath("$.borrower.kpp").value(companyDto.getKpp()),
                        jsonPath("$.borrower.inn").value(companyDto.getInn()),
                        jsonPath("$.info.id").value(loanRequestInfo.getId()),
                        jsonPath("$.info.status").value(loanRequestInfo.getStatus().name()),
                        jsonPath("$.info.term").value(loanRequestInfo.getTerm()),
                        jsonPath("$.info.maxRate").value(loanRequestInfo.getMaxRate()),
                        jsonPath("$.info.dateCreate").value("2021-05-12"),
                        jsonPath("$.info.sum.value").value(loanRequestInfo.getSum().getValue()),
                        jsonPath("$.info.sum.unit").value(loanRequestInfo.getSum().getUnit().name()),
                        jsonPath("$.info.dateIssue").value("2021-08-04"),
                        jsonPath("$.banks", hasSize(2)),
                        jsonPath("$.banks[0].approveBankAgent").value(bank1.isApproveBankAgent()),
                        jsonPath("$.banks[0].id").value(bank1.getId()),
                        jsonPath("$.banks[0].name").value(bank1.getName()),
                        jsonPath("$.banks[0].sum.value").value(bank1.getSum().getValue()),
                        jsonPath("$.banks[0].sum.unit").value(bank1.getSum().getUnit().name()),
                        jsonPath("$.banks[0].issuedSum").value(bank1.getIssuedSum()),
                        jsonPath("$.banks[1].approveBankAgent").value(bank2.isApproveBankAgent()),
                        jsonPath("$.banks[1].id").value(bank2.getId()),
                        jsonPath("$.banks[1].name").value(bank2.getName()),
                        jsonPath("$.banks[1].sum.value").value(bank2.getSum().getValue()),
                        jsonPath("$.banks[1].sum.unit").value(bank2.getSum().getUnit().name()),
                        jsonPath("$.banks[1].issuedSum").value(bank2.getIssuedSum())
                );

        verify(userService, times(2)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(loanRequestService, times(1)).getOwnedCompanyLoanRequestById(requestId, company);

        verify(loanRequestConverter, times(1)).convert(loanRequest);
    }

    @Test
    @DisplayName("Получение участников синдиката по id заявки без авторизации пользователя")
    void getSyndicateParticipantsByRequestIdWithAccessDeniedTest() throws Exception {
        long loanRequestId = 12L;

        mockMvc.perform(get(BASE_PATH + "/{id}/participants", loanRequestId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(blankOrNullString()));

        verify(syndicateParticipantConverter, never()).convert(any());
        verify(syndicateParticipantService, never()).getSyndicateParticipantsByRequestId(anyLong());
    }

    @Test
    @DisplayName("Получение участников синдиката по id несуществующей заявки")
    @WithMockUser
    void getSyndicateParticipantsByRequestIdNotFoundTest() throws Exception {
        long loanRequestId = 12L;

        when(syndicateParticipantService.getSyndicateParticipantsByRequestId(loanRequestId))
                .thenThrow(LoanRequestNotFoundException.class);

        mockMvc.perform(get(BASE_PATH + "/{id}/participants", loanRequestId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.code").value(404),
                        jsonPath("$.status").value("NOT_FOUND"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(syndicateParticipantConverter, never()).convert(any());
        verify(syndicateParticipantService, times(1)).getSyndicateParticipantsByRequestId(loanRequestId);
    }

    @Test
    @DisplayName("Получение участников синдиката по id заявки")
    @WithMockUser
    void getSyndicateParticipantsByRequestIdTest() throws Exception {
        long requestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        SyndicateParticipant syndicateParticipant1 = new SyndicateParticipant();
        SyndicateParticipant syndicateParticipant2 = new SyndicateParticipant();

        BankParticipantInfo bank1 = BankParticipantInfo.builder()
                .approveBankAgent(true)
                .id(1666L)
                .name("Sber")
                .sum(LoanSum.valueOf(3_000_000))
                .issuedSum(3_000_000)
                .build();
        BankParticipantInfo bank2 = BankParticipantInfo.builder()
                .approveBankAgent(false)
                .id(1622L)
                .name("Vtb")
                .sum(LoanSum.valueOf(200_000))
                .issuedSum(100_000)
                .build();

        when(syndicateParticipantService.getSyndicateParticipantsByRequestId(requestId))
                .thenReturn(List.of(syndicateParticipant1, syndicateParticipant2));

        when(syndicateParticipantConverter.convert(syndicateParticipant1)).thenReturn(bank1);
        when(syndicateParticipantConverter.convert(syndicateParticipant2)).thenReturn(bank2);

        mockMvc.perform(get(BASE_PATH + "/{id}/participants", requestId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$", hasSize(2)),
                        jsonPath("$[0].approveBankAgent").value(bank1.isApproveBankAgent()),
                        jsonPath("$[0].id").value(bank1.getId()),
                        jsonPath("$[0].name").value(bank1.getName()),
                        jsonPath("$[0].sum.value").value(bank1.getSum().getValue()),
                        jsonPath("$[0].sum.unit").value(bank1.getSum().getUnit().name()),
                        jsonPath("$[0].issuedSum").value(bank1.getIssuedSum()),
                        jsonPath("$[1].approveBankAgent").value(bank2.isApproveBankAgent()),
                        jsonPath("$[1].id").value(bank2.getId()),
                        jsonPath("$[1].name").value(bank2.getName()),
                        jsonPath("$[1].sum.value").value(bank2.getSum().getValue()),
                        jsonPath("$[1].sum.unit").value(bank2.getSum().getUnit().name()),
                        jsonPath("$[1].issuedSum").value(bank2.getIssuedSum())
                );

        verify(syndicateParticipantService, times(1)).getSyndicateParticipantsByRequestId(requestId);
        verify(syndicateParticipantConverter, times(1)).convert(syndicateParticipant1);
        verify(syndicateParticipantConverter, times(1)).convert(syndicateParticipant2);
    }

    @Test
    @DisplayName("Получение банком всех заявок без авторизации пользователя")
    void getAllRequestsWithAccessDeniedTest() throws Exception {

        mockMvc.perform(get(BASE_PATH + "/all"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(blankOrNullString()));

        verify(userService, never()).getCurrentUser(any());
        verify(loanRequestService, never()).getAll(any());
        verify(loanRequestCollectionConverter, never()).convert(any());
    }

    @Test
    @DisplayName("Получение всех заявок, пользователь не является банком")
    @WithMockUser(roles = "COMPANY")
    void getAllRequestsWithNoBankUserTest() throws Exception {

        mockMvc.perform(get(BASE_PATH + "/all"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.code").value(403),
                        jsonPath("$.status").value("FORBIDDEN"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(userService, never()).getCurrentUser(any());
        verify(loanRequestService, never()).getAll(any());
        verify(loanRequestCollectionConverter, never()).convert(any());
    }

    @Test
    @DisplayName("Получение всех заявок банком")
    @WithMockUser(roles = "BANK")
    void getAllRequestsTest() throws Exception {
        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        CompanyDto ownCompanyDto = CompanyDto.builder()
                .id(10L)
                .fullName("comFullName1")
                .shortName("comShortName1")
                .actualAddress("comActAddress1")
                .legalAddress("comLegAddress1")
                .inn("1234567890")
                .kpp("111111111")
                .build();

        LoanRequestInfo ownLoanRequestInfo = LoanRequestInfo.builder()
                .id(133L)
                .dateCreate(LocalDate.of(2022, Month.JUNE, 17))
                .maxRate(0.2)
                .sum(LoanSum.valueOf(123_230))
                .status(LoanRequestStatus.OPEN)
                .term(3)
                .build();

        CompanyDto otherCompanyDto = CompanyDto.builder()
                .id(123L)
                .fullName("comFullName2")
                .shortName("comShortName2")
                .actualAddress("comActAddress2")
                .legalAddress("comLegAddress2")
                .inn("3232424")
                .kpp("155453")
                .build();

        LoanRequestInfo otherLoanRequestInfo = LoanRequestInfo.builder()
                .id(323L)
                .dateCreate(LocalDate.of(2021, Month.MAY, 12))
                .maxRate(0.4)
                .sum(LoanSum.valueOf(3_323_000))
                .status(LoanRequestStatus.CLOSE)
                .term(12)
                .dateIssue(LocalDate.of(2021, Month.AUGUST, 4))
                .build();

        BankParticipantInfo bank1 = BankParticipantInfo.builder()
                .approveBankAgent(true)
                .id(1666L)
                .name("Sber")
                .sum(LoanSum.valueOf(3_000_000))
                .issuedSum(3_000_000)
                .build();
        BankParticipantInfo bank2 = BankParticipantInfo.builder()
                .approveBankAgent(false)
                .id(1622L)
                .name("Vtb")
                .sum(LoanSum.valueOf(200_000))
                .issuedSum(100_000)
                .build();

        LoanRequestResponse otherRequestResponse = LoanRequestResponse.builder()
                .borrower(otherCompanyDto)
                .banks(List.of(bank1, bank2))
                .info(otherLoanRequestInfo)
                .build();
        LoanRequestResponse ownRequestResponse = LoanRequestResponse.builder()
                .borrower(ownCompanyDto)
                .banks(Collections.emptyList())
                .info(ownLoanRequestInfo)
                .build();

        LoanRequestCollection loanRequestCollection = new LoanRequestCollection();

        LoanRequestCollectionResponse collectionResponse = new LoanRequestCollectionResponse();
        collectionResponse.addOther(otherRequestResponse);
        collectionResponse.addOwn(ownRequestResponse);

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanRequestService.getAll(company)).thenReturn(loanRequestCollection);
        when(loanRequestCollectionConverter.convert(loanRequestCollection)).thenReturn(collectionResponse);

        mockMvc.perform(get(BASE_PATH + "/all"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.own", hasSize(1)),
                        jsonPath("$.other", hasSize(1)),
                        jsonPath("$.own[0].banks", hasSize(0)),
                        jsonPath("$.own[0].borrower.id").value(ownCompanyDto.getId()),
                        jsonPath("$.own[0].borrower.fullName").value(ownCompanyDto.getFullName()),
                        jsonPath("$.own[0].borrower.shortName").value(ownCompanyDto.getShortName()),
                        jsonPath("$.own[0].borrower.actualAddress").value(ownCompanyDto.getActualAddress()),
                        jsonPath("$.own[0].borrower.legalAddress").value(ownCompanyDto.getLegalAddress()),
                        jsonPath("$.own[0].borrower.kpp").value(ownCompanyDto.getKpp()),
                        jsonPath("$.own[0].borrower.inn").value(ownCompanyDto.getInn()),
                        jsonPath("$.own[0].info.id").value(ownLoanRequestInfo.getId()),
                        jsonPath("$.own[0].info.status").value(ownLoanRequestInfo.getStatus().name()),
                        jsonPath("$.own[0].info.term").value(ownLoanRequestInfo.getTerm()),
                        jsonPath("$.own[0].info.maxRate").value(ownLoanRequestInfo.getMaxRate()),
                        jsonPath("$.own[0].info.dateCreate").value("2022-06-17"),
                        jsonPath("$.own[0].info.sum.value").value(ownLoanRequestInfo.getSum().getValue()),
                        jsonPath("$.own[0].info.sum.unit").value(ownLoanRequestInfo.getSum().getUnit().name()),
                        jsonPath("$.own[0].info.dateIssue").hasJsonPath(),
                        jsonPath("$.other[0].borrower.id").value(otherCompanyDto.getId()),
                        jsonPath("$.other[0].borrower.fullName").value(otherCompanyDto.getFullName()),
                        jsonPath("$.other[0].borrower.shortName").value(otherCompanyDto.getShortName()),
                        jsonPath("$.other[0].borrower.actualAddress").value(otherCompanyDto.getActualAddress()),
                        jsonPath("$.other[0].borrower.legalAddress").value(otherCompanyDto.getLegalAddress()),
                        jsonPath("$.other[0].borrower.kpp").value(otherCompanyDto.getKpp()),
                        jsonPath("$.other[0].borrower.inn").value(otherCompanyDto.getInn()),
                        jsonPath("$.other[0].info.id").value(otherLoanRequestInfo.getId()),
                        jsonPath("$.other[0].info.status").value(otherLoanRequestInfo.getStatus().name()),
                        jsonPath("$.other[0].info.term").value(otherLoanRequestInfo.getTerm()),
                        jsonPath("$.other[0].info.maxRate").value(otherLoanRequestInfo.getMaxRate()),
                        jsonPath("$.other[0].info.dateCreate").value("2021-05-12"),
                        jsonPath("$.other[0].info.sum.value").value(otherLoanRequestInfo.getSum().getValue()),
                        jsonPath("$.other[0].info.sum.unit").value(otherLoanRequestInfo.getSum().getUnit().name()),
                        jsonPath("$.other[0].info.dateIssue").value("2021-08-04"),
                        jsonPath("$.other[0].banks", hasSize(2)),
                        jsonPath("$.other[0].banks[0].approveBankAgent").value(bank1.isApproveBankAgent()),
                        jsonPath("$.other[0].banks[0].id").value(bank1.getId()),
                        jsonPath("$.other[0].banks[0].name").value(bank1.getName()),
                        jsonPath("$.other[0].banks[0].sum.value").value(bank1.getSum().getValue()),
                        jsonPath("$.other[0].banks[0].sum.unit").value(bank1.getSum().getUnit().name()),
                        jsonPath("$.other[0].banks[0].issuedSum").value(bank1.getIssuedSum()),
                        jsonPath("$.other[0].banks[1].approveBankAgent").value(bank2.isApproveBankAgent()),
                        jsonPath("$.other[0].banks[1].id").value(bank2.getId()),
                        jsonPath("$.other[0].banks[1].name").value(bank2.getName()),
                        jsonPath("$.other[0].banks[1].sum.value").value(bank2.getSum().getValue()),
                        jsonPath("$.other[0].banks[1].sum.unit").value(bank2.getSum().getUnit().name()),
                        jsonPath("$.other[0].banks[1].issuedSum").value(bank2.getIssuedSum())
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(loanRequestService, times(1)).getAll(company);
        verify(loanRequestCollectionConverter, times(1)).convert(loanRequestCollection);
    }

    @Test
    @DisplayName("Удаление заявки без авторизации пользователя")
    void deleteRequestByIdWithAccessDeniedTest() throws Exception {
        long loanRequestId = 12L;

        mockMvc.perform(delete(BASE_PATH + "/{id}", loanRequestId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(blankOrNullString()));

        verify(userService, never()).getCurrentUser(any());
        verify(loanRequestService, never()).getOwnedCompanyLoanRequestById(anyLong(), any());
        verify(loanRequestService, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Удаление заявки не принадлежащей пользователю")
    @WithMockUser
    void deleteRequestByIdWithNotOwnedTest() throws Exception {
        long requestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanRequestService.getOwnedCompanyLoanRequestById(requestId, company))
                .thenThrow(ForbiddenResourceException.class);

        mockMvc.perform(delete(BASE_PATH + "/{id}", requestId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.code").value(403),
                        jsonPath("$.status").value("FORBIDDEN"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(loanRequestService, times(1)).getOwnedCompanyLoanRequestById(requestId, company);

        verify(loanRequestService, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Удаление заявки по id несуществующей заявки")
    @WithMockUser
    void deleteRequestByIdNotFoundTest() throws Exception {
        long requestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        doThrow(LoanRequestNotFoundException.class).when(loanRequestService).deleteById(requestId);

        mockMvc.perform(delete(BASE_PATH + "/{id}", requestId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.code").value(404),
                        jsonPath("$.status").value("NOT_FOUND"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(loanRequestService, times(1)).getOwnedCompanyLoanRequestById(requestId, company);

        verify(loanRequestService, times(1)).deleteById(requestId);
    }

    @Test
    @DisplayName("Удаление заявки по id")
    @WithMockUser
    void deleteRequestByIdTest() throws Exception {
        long requestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);

        mockMvc.perform(delete(BASE_PATH + "/{id}", requestId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(blankOrNullString()));

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(loanRequestService, times(1)).getOwnedCompanyLoanRequestById(requestId, company);

        verify(loanRequestService, times(1)).deleteById(requestId);
    }
}