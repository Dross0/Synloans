package com.synloans.loans.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synloans.loans.adapter.dto.LoanCreateRequest;
import com.synloans.loans.adapter.dto.LoanId;
import com.synloans.loans.adapter.dto.NodeUserInfo;
import com.synloans.loans.adapter.exception.LoanCreateException;
import com.synloans.loans.adapter.service.LoanAdapterService;
import com.synloans.loans.adapter.utils.InvalidNodeUserInfoProvider;
import com.synloans.loans.adapter.utils.JsonHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = LoanController.class)
@ActiveProfiles("test")
class LoanControllerTest {

    private static final String BASE_PATH = "/loans";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    LoanAdapterService loanAdapterService;


    @Test
    @DisplayName("Ошибка создание кредита")
    void createLoanErrorTest() throws Exception {
        NodeUserInfo nodeUserInfo = new NodeUserInfo(
                "address-test",
                "user-test",
                "password-test"
        );
        LoanCreateRequest loanCreateRequest = new LoanCreateRequest();
        loanCreateRequest.setBankAgent(nodeUserInfo);
        loanCreateRequest.setLoanSum(100L);
        loanCreateRequest.setTerm(1);
        loanCreateRequest.setRate(0.23);
        loanCreateRequest.setBorrower("Nox");
        loanCreateRequest.setBanks(Arrays.asList("Sber", "VTB"));

        doThrow(LoanCreateException.class).when(loanAdapterService).createLoan(loanCreateRequest);

        mockMvc.perform(
                post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.asJsonString(objectMapper, loanCreateRequest))
        )
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").hasJsonPath())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(loanAdapterService, times(1)).createLoan(loanCreateRequest);
    }

    @Test
    @DisplayName("Успешное создание кредита")
    void createLoanTest() throws Exception {
        NodeUserInfo nodeUserInfo = new NodeUserInfo(
                "address-test",
                "user-test",
                "password-test"
        );
        LoanCreateRequest loanCreateRequest = new LoanCreateRequest();
        loanCreateRequest.setBankAgent(nodeUserInfo);
        loanCreateRequest.setLoanSum(100L);
        loanCreateRequest.setTerm(1);
        loanCreateRequest.setRate(0.23);
        loanCreateRequest.setBorrower("Nox");
        loanCreateRequest.setBanks(Arrays.asList("Sber", "VTB"));

        LoanId loanId = new LoanId("ecece", UUID.randomUUID());

        when(loanAdapterService.createLoan(loanCreateRequest)).thenReturn(loanId);

        mockMvc.perform(
                post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.asJsonString(objectMapper, loanCreateRequest))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanExternalId").value(loanId.getLoanExternalId()))
                .andExpect(jsonPath("$.id").value(loanId.getId().toString()));

        verify(loanAdapterService, times(1)).createLoan(loanCreateRequest);
    }

    @ParameterizedTest
    @MethodSource("notValidCreateRequest")
    @DisplayName("Создание кредита с невалидным запросом")
    void createLoanWithInvalidDataTest(LoanCreateRequest loanCreateRequest) throws Exception {
        mockMvc.perform(
                post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.asJsonString(objectMapper, loanCreateRequest))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").hasJsonPath())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(loanAdapterService, never()).createLoan(any());
    }

    private static LoanCreateRequest brokenCreateRequest(Consumer<LoanCreateRequest> breakAction){
        NodeUserInfo nodeUserInfo = new NodeUserInfo(
                "address-test",
                "user-test",
                "password-test"
        );
        LoanCreateRequest loanCreateRequest = new LoanCreateRequest();
        loanCreateRequest.setBankAgent(nodeUserInfo);
        loanCreateRequest.setLoanSum(100L);
        loanCreateRequest.setTerm(1);
        loanCreateRequest.setRate(0.23);
        loanCreateRequest.setBorrower("Nox");
        loanCreateRequest.setBanks(Arrays.asList("Sber", "VTB"));

        breakAction.accept(loanCreateRequest);
        return loanCreateRequest;
    }

    private static Stream<Arguments> notValidCreateRequest() {

        Stream<Arguments> requestsWithInvalidNodeInfo = InvalidNodeUserInfoProvider.invalidNodes()
                .stream()
                .map(nodeUserInfo -> Arguments.of(brokenCreateRequest(r -> r.setBankAgent(nodeUserInfo))));

        return Stream.concat(
                requestsWithInvalidNodeInfo,
                Stream.of(
                        Arguments.of(
                                brokenCreateRequest(r -> r.setBankAgent(null))
                        ),
                        Arguments.of(
                                brokenCreateRequest(r -> r.setBorrower(null))
                        ),
                        Arguments.of(
                                brokenCreateRequest(r -> r.setBorrower(""))
                        ),
                        Arguments.of(
                                brokenCreateRequest(r -> r.setLoanSum(-123L))
                        ),
                        Arguments.of(
                                brokenCreateRequest(r -> r.setTerm(-1))
                        ),
                        Arguments.of(
                                brokenCreateRequest(r -> r.setRate(-10.0))
                        ),
                        Arguments.of(
                                brokenCreateRequest(r -> r.setBanks(null))
                        ),
                        Arguments.of(
                                brokenCreateRequest(r -> r.setBanks(Collections.emptyList()))
                        )
        ));
    }


}