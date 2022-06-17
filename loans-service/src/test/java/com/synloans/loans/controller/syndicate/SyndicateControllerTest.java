package com.synloans.loans.controller.syndicate;

import com.synloans.loans.controller.BaseControllerTest;
import com.synloans.loans.model.dto.SyndicateJoinRequest;
import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.service.company.BankService;
import com.synloans.loans.service.exception.SyndicateQuitException;
import com.synloans.loans.service.exception.notfound.LoanRequestNotFoundException;
import com.synloans.loans.service.syndicate.SyndicateParticipantService;
import com.synloans.loans.service.syndicate.SyndicateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.JsonHelper.asJsonString;

@WebMvcTest(
        value = SyndicateController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = NoConvertersFilter.class)
)
class SyndicateControllerTest extends BaseControllerTest {

    private static final String BASE_PATH = "/syndicates";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BankService bankService;

    @MockBean
    SyndicateService syndicateService;

    @MockBean
    SyndicateParticipantService syndicateParticipantService;

    @Captor
    ArgumentCaptor<Authentication> authenticationArgumentCaptor;

    @Test
    @DisplayName("Попытка не банка присоединться к синдикату")
    @WithMockUser(username = "user", roles = "COMPANY")
    void joinToSyndicateWithoutBankRoleTest() throws Exception {
        SyndicateJoinRequest joinRequest = new SyndicateJoinRequest(
                12L,
                100_000L,
                true
        );

        mockMvc.perform(
                post(BASE_PATH + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, joinRequest))
        )
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpectAll(
                        jsonPath("$.code").value(403),
                        jsonPath("$.status").value("FORBIDDEN"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").exists(),
                        jsonPath("$.timestamp").exists()
                );

        verify(syndicateService, never()).joinBankToSyndicate(any(), any());
        verify(userService, never()).getCurrentUser(any());
        verify(bankService, never()).getByCompany(any());
    }

    @Test
    @DisplayName("Присоединение к синдикату по несуществующей заявке")
    @WithMockUser(username = "user", roles = "BANK")
    void joinToSyndicateLoanRequestNotFoundTest() throws Exception {
        SyndicateJoinRequest joinRequest = new SyndicateJoinRequest(
                12L,
                100_000L,
                true
        );

        User user = new User();
        Company company = new Company();
        user.setCompany(company);
        Bank bank = new Bank();

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(bankService.getByCompany(company)).thenReturn(bank);
        when(syndicateService.joinBankToSyndicate(joinRequest, bank)).thenThrow(LoanRequestNotFoundException.class);

        mockMvc.perform(
                post(BASE_PATH + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, joinRequest))
        )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpectAll(
                        jsonPath("$.code").value(404),
                        jsonPath("$.status").value("NOT_FOUND"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(bankService, times(1)).getByCompany(company);
        verify(syndicateService, times(1)).joinBankToSyndicate(joinRequest, bank);
    }

    @Test
    @DisplayName("Присоединение к синдикату если банк не найден")
    @WithMockUser(username = "user", roles = "BANK")
    void joinToSyndicateBankNotFoundTest() throws Exception {
        SyndicateJoinRequest joinRequest = new SyndicateJoinRequest(
                12L,
                100_000L,
                true
        );

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(bankService.getByCompany(company)).thenReturn(null);

        mockMvc.perform(
                post(BASE_PATH + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, joinRequest))
        )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpectAll(
                        jsonPath("$.code").value(404),
                        jsonPath("$.status").value("NOT_FOUND"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(bankService, times(1)).getByCompany(company);
        verify(syndicateService, never()).joinBankToSyndicate(any(), any());
    }

    @Test
    @DisplayName("Ошибка при присоединении к синдикату")
    @WithMockUser(username = "user", roles = "BANK")
    void joinToSyndicateFailedTest() throws Exception {
        SyndicateJoinRequest joinRequest = new SyndicateJoinRequest(
                12L,
                100_000L,
                true
        );

        User user = new User();
        Company company = new Company();
        user.setCompany(company);
        Bank bank = new Bank();

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(bankService.getByCompany(company)).thenReturn(bank);
        when(syndicateService.joinBankToSyndicate(joinRequest, bank)).thenReturn(Optional.empty());

        mockMvc.perform(
                post(BASE_PATH + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, joinRequest))
        )
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpectAll(
                        jsonPath("$.code").value(500),
                        jsonPath("$.status").value("INTERNAL_SERVER_ERROR"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(bankService, times(1)).getByCompany(company);
        verify(syndicateService, times(1)).joinBankToSyndicate(joinRequest, bank);
    }

    @Test
    @DisplayName("Присоединение к синдикату с невалидным запросом")
    @WithMockUser(username = "user", roles = "BANK")
    void joinToSyndicateWithInvalidDataTest() throws Exception {
        SyndicateJoinRequest joinRequest = new SyndicateJoinRequest(
                12L,
                -100_000L,
                true
        );

        mockMvc.perform(
                post(BASE_PATH + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, joinRequest))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpectAll(
                        jsonPath("$.code").value(400),
                        jsonPath("$.status").value("BAD_REQUEST"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(userService, never()).getCurrentUser(any());

        verify(bankService, never()).getByCompany(any());
        verify(syndicateService, never()).joinBankToSyndicate(any(), any());
    }

    @Test
    @DisplayName("Присоединение к синдикату")
    @WithMockUser(username = "user", roles = "BANK")
    void joinToSyndicateTest() throws Exception {
        SyndicateJoinRequest joinRequest = new SyndicateJoinRequest(
                12L,
                100_000L,
                true
        );

        User user = new User();
        Company company = new Company();
        user.setCompany(company);
        Bank bank = new Bank();

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(bankService.getByCompany(company)).thenReturn(bank);
        when(syndicateService.joinBankToSyndicate(joinRequest, bank))
                .thenReturn(Optional.of(new SyndicateParticipant()));

        mockMvc.perform(
                post(BASE_PATH + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, joinRequest))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(blankOrNullString()));

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(bankService, times(1)).getByCompany(company);
        verify(syndicateService, times(1)).joinBankToSyndicate(joinRequest, bank);
    }

    @Test
    @DisplayName("Присоединение к синдикату без авторизации пользователя")
    void joinToSyndicateWithAccessDeniedTest() throws Exception {
        SyndicateJoinRequest joinRequest = new SyndicateJoinRequest(
                12L,
                100_000L,
                true
        );

        mockMvc.perform(
                post(BASE_PATH + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, joinRequest))
        )
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(blankOrNullString()));

        verify(userService, never()).getCurrentUser(any());

        verify(bankService, never()).getByCompany(any());
        verify(syndicateService, never()).joinBankToSyndicate(any(), any());
    }

    @Test
    @DisplayName("Попытка не банка выйти из синдиката")
    @WithMockUser(username = "user", roles = "COMPANY")
    void quitFromSyndicateWithoutBankRoleTest() throws Exception {
        long loanRequestId = 12L;

        mockMvc.perform(delete(BASE_PATH + "/{loanRequestId}", loanRequestId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpectAll(
                        jsonPath("$.code").value(403),
                        jsonPath("$.status").value("FORBIDDEN"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").exists(),
                        jsonPath("$.timestamp").exists()
                );

        verify(syndicateParticipantService, never()).quitFromSyndicate(any(), any());
        verify(userService, never()).getCurrentUser(any());
        verify(bankService, never()).getByCompany(any());
    }

    @Test
    @DisplayName("Выход из синдиката если банк не найден")
    @WithMockUser(username = "user", roles = "BANK")
    void quitFromSyndicateBankNotFoundTest() throws Exception {
        long loanRequestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(bankService.getByCompany(company)).thenReturn(null);

        mockMvc.perform(delete(BASE_PATH + "/{loanRequestId}", loanRequestId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpectAll(
                        jsonPath("$.code").value(404),
                        jsonPath("$.status").value("NOT_FOUND"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(bankService, times(1)).getByCompany(company);
        verify(syndicateParticipantService, never()).quitFromSyndicate(any(), any());
    }

    @Test
    @DisplayName("Выход из синдиката при выданном кредите")
    @WithMockUser(username = "user", roles = "BANK")
    void quitFromSyndicateWhenLoanIssuedTest() throws Exception {
        long loanRequestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);
        Bank bank = new Bank();

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(bankService.getByCompany(company)).thenReturn(bank);
        doThrow(SyndicateQuitException.class)
                .when(syndicateParticipantService)
                .quitFromSyndicate(loanRequestId, bank);


        mockMvc.perform(delete(BASE_PATH + "/{loanRequestId}", loanRequestId))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpectAll(
                        jsonPath("$.code").value(500),
                        jsonPath("$.status").value("INTERNAL_SERVER_ERROR"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(bankService, times(1)).getByCompany(company);
        verify(syndicateParticipantService, times(1)).quitFromSyndicate(loanRequestId, bank);
    }


    @Test
    @DisplayName("Успешный выход из синдиката")
    @WithMockUser(username = "user", roles = "BANK")
    void quitFromSyndicateTest() throws Exception {
        long loanRequestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);
        Bank bank = new Bank();

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(bankService.getByCompany(company)).thenReturn(bank);


        mockMvc.perform(delete(BASE_PATH + "/{loanRequestId}", loanRequestId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(blankOrNullString()));

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(bankService, times(1)).getByCompany(company);
        verify(syndicateParticipantService, times(1)).quitFromSyndicate(loanRequestId, bank);
    }

    @Test
    @DisplayName("Выход из синдиката без авторизации пользователя")
    void quitFromSyndicateWithAccessDeniedTest() throws Exception {
        long loanRequestId = 12L;

        mockMvc.perform(delete(BASE_PATH + "/{loanRequestId}", loanRequestId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(blankOrNullString()));

        verify(userService, never()).getCurrentUser(any());


        verify(bankService, never()).getByCompany(any());
        verify(syndicateParticipantService, never()).quitFromSyndicate(any(), any());
    }
}