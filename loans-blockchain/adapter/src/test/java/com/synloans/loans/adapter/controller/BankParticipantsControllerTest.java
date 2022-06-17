package com.synloans.loans.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synloans.loans.adapter.dto.BankJoinRequest;
import com.synloans.loans.adapter.dto.LoanId;
import com.synloans.loans.adapter.dto.NodeUserInfo;
import com.synloans.loans.adapter.exception.BankJoinException;
import com.synloans.loans.adapter.service.LoanAdapterService;
import com.synloans.loans.adapter.utils.InvalidNodeUserInfoProvider;
import com.synloans.loans.adapter.utils.JsonHelper;
import org.hamcrest.Matchers;
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

import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = BankParticipantsController.class)
@ActiveProfiles("test")
class BankParticipantsControllerTest {

    private static final String BASE_PATH = "/banks";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    LoanAdapterService loanAdapterService;


    @Test
    @DisplayName("Ошибка создание участника синдиката")
    void joinBankErrorTest() throws Exception {
        NodeUserInfo nodeUserInfo = new NodeUserInfo(
                "address-test",
                "user-test",
                "password-test"
        );
        LoanId loanId = new LoanId("rere", UUID.randomUUID());
        BankJoinRequest joinRequest = new BankJoinRequest(
                nodeUserInfo,
                loanId,
                10030
        );

        doThrow(BankJoinException.class).when(loanAdapterService).joinBank(joinRequest);

        mockMvc.perform(
                post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.asJsonString(objectMapper, joinRequest))
        )
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").hasJsonPath())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(loanAdapterService, times(1)).joinBank(joinRequest);
    }

    @Test
    @DisplayName("Успешное присоединение участника синдиката")
    void joinBankTest() throws Exception {
        NodeUserInfo nodeUserInfo = new NodeUserInfo(
                "address-test",
                "user-test",
                "password-test"
        );
        LoanId loanId = new LoanId("rere", UUID.randomUUID());
        BankJoinRequest joinRequest = new BankJoinRequest(
                nodeUserInfo,
                loanId,
                10030
        );

        mockMvc.perform(
                post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.asJsonString(objectMapper, joinRequest))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.isEmptyOrNullString()));

        verify(loanAdapterService, times(1)).joinBank(joinRequest);
    }

    @ParameterizedTest
    @MethodSource("notValidJoinRequest")
    @DisplayName("Присоединение участника синдиката с невалидным запросом")
    void joinBankWithInvalidDataTest(BankJoinRequest bankJoinRequest) throws Exception {
        mockMvc.perform(
                post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.asJsonString(objectMapper, bankJoinRequest))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").hasJsonPath())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(loanAdapterService, never()).joinBank(any());
    }

    private static Stream<Arguments> notValidJoinRequest() {
        NodeUserInfo validNodeUserInfo = new NodeUserInfo(
                "address-test",
                "user-test",
                "password-test"
        );
        LoanId validLoanId = new LoanId("rere", UUID.randomUUID());

        Stream<Arguments> requestsWithInvalidNodeInfo = InvalidNodeUserInfoProvider.invalidNodes()
                .stream()
                .map(nodeUserInfo -> Arguments.of(new BankJoinRequest(nodeUserInfo, validLoanId, 123L)));

        return Stream.concat(
                requestsWithInvalidNodeInfo,
                Stream.of(
                Arguments.of(
                        new BankJoinRequest(
                                validNodeUserInfo,
                                validLoanId,
                                -123L
                        )
                ),
                Arguments.of(
                        new BankJoinRequest(
                                validNodeUserInfo,
                                new LoanId("d", null),
                                123L
                        )
                ),
                Arguments.of(
                        new BankJoinRequest(
                                validNodeUserInfo,
                                null,
                                123L
                        )
                ),
                Arguments.of(
                        new BankJoinRequest(
                                null,
                                validLoanId,
                                123L
                        )
                ),
                Arguments.of(
                        new BankJoinRequest(
                                null,
                                null,
                                123L
                        )
                )
        ));
    }


}