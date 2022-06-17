package com.synloans.loans.controller.loan;

import com.synloans.loans.controller.BaseControllerTest;
import com.synloans.loans.mapper.converter.payments.ActualPaymentConverter;
import com.synloans.loans.mapper.converter.payments.PlannedPaymentConverter;
import com.synloans.loans.model.dto.loan.payments.ActualPaymentDto;
import com.synloans.loans.model.dto.loan.payments.PaymentRequest;
import com.synloans.loans.model.dto.loan.payments.PlannedPaymentDto;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.loan.Loan;
import com.synloans.loans.model.entity.loan.payment.ActualPayment;
import com.synloans.loans.model.entity.loan.payment.PlannedPayment;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.service.exception.ForbiddenResourceException;
import com.synloans.loans.service.exception.InvalidLoanRequestException;
import com.synloans.loans.service.exception.blockchain.BlockchainPersistException;
import com.synloans.loans.service.exception.notfound.LoanNotFoundException;
import com.synloans.loans.service.exception.notfound.LoanRequestNotFoundException;
import com.synloans.loans.service.exception.notfound.NodeNotFoundException;
import com.synloans.loans.service.loan.LoanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.JsonHelper.asJsonString;

@WebMvcTest(
        value = LoanController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = NoConvertersFilter.class)
)
class LoanControllerTest extends BaseControllerTest {

    private static final String BASE_PATH = "/loans";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    LoanService loanService;

    @MockBean
    PlannedPaymentConverter plannedPaymentConverter;

    @MockBean
    ActualPaymentConverter actualPaymentConverter;

    @Captor
    ArgumentCaptor<Authentication> authenticationArgumentCaptor;

    @Test
    @DisplayName("Старт кредита без авторизации пользователя")
    void startLoanWithAccessDeniedTest() throws Exception {
        long requestId = 12L;

        mockMvc.perform(post(BASE_PATH + "/{loanRequestId}/start", requestId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(blankOrNullString()));

        verify(userService, never()).getCurrentUser(any());
        verify(loanService, never()).startLoanByRequestId(anyLong(), any());
    }

    @Test
    @DisplayName("Старт кредита не от создателя заявки")
    @WithMockUser
    void startLoanByNotOwnerTest() throws Exception {
        long loanRequestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanService.startLoanByRequestId(loanRequestId, user)).thenThrow(ForbiddenResourceException.class);

        mockMvc.perform(post(BASE_PATH + "/{loanRequestId}/start", loanRequestId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpectAll(
                        jsonPath("$.code").value(403),
                        jsonPath("$.status").value("FORBIDDEN"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(loanService, times(1)).startLoanByRequestId(loanRequestId, user);
    }

    @Test
    @DisplayName("Старт кредита, который не готов к выдаче")
    @WithMockUser
    void startLoanWithInvalidStatusTest() throws Exception {
        long loanRequestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanService.startLoanByRequestId(loanRequestId, user)).thenThrow(InvalidLoanRequestException.class);

        mockMvc.perform(post(BASE_PATH + "/{loanRequestId}/start", loanRequestId))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpectAll(
                        jsonPath("$.code").value(400),
                        jsonPath("$.status").value("BAD_REQUEST"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(loanService, times(1)).startLoanByRequestId(loanRequestId, user);
    }

    @Test
    @DisplayName("Старт кредита без кредитной заявки")
    @WithMockUser
    void startLoanWithoutLoanRequestTest() throws Exception {
        long loanRequestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanService.startLoanByRequestId(loanRequestId, user)).thenThrow(LoanRequestNotFoundException.class);

        mockMvc.perform(post(BASE_PATH + "/{loanRequestId}/start", loanRequestId))
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

        verify(loanService, times(1)).startLoanByRequestId(loanRequestId, user);
    }

    @Test
    @DisplayName("Ошибка при сохранении начатого кредита в блокчейн сеть")
    @WithMockUser
    void startLoanPersistBlockchainFailedTest() throws Exception {
        long loanRequestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanService.startLoanByRequestId(loanRequestId, user)).thenThrow(BlockchainPersistException.class);

        mockMvc.perform(post(BASE_PATH + "/{loanRequestId}/start", loanRequestId))
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

        verify(loanService, times(1)).startLoanByRequestId(loanRequestId, user);
    }

    @Test
    @DisplayName("Старт кредита")
    @WithMockUser
    void startLoanTest() throws Exception {
        long loanRequestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        Loan loan = new Loan();
        loan.setId(1100L);

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanService.startLoanByRequestId(loanRequestId, user)).thenReturn(loan);

        mockMvc.perform(post(BASE_PATH + "/{loanRequestId}/start", loanRequestId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.loanRequestId").value(loanRequestId),
                        jsonPath("$.loanId").value(loan.getId())
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(loanService, times(1)).startLoanByRequestId(loanRequestId, user);
    }

    @Test
    @DisplayName("Получение плановых платежей без авторизации пользователя")
    void getPlannedPaymentsWithAccessDeniedTest() throws Exception {
        long requestId = 12L;

        mockMvc.perform(get(BASE_PATH + "/{loanRequestId}/payments/plan", requestId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(blankOrNullString()));

        verify(userService, never()).getCurrentUser(any());
        verify(loanService, never()).getPlannedPaymentsByRequestId(anyLong(), any());
        verify(plannedPaymentConverter, never()).convert(any());
    }

    @Test
    @DisplayName("Получение плановых платежей не участником кредита")
    @WithMockUser
    void getPlannedPaymentsByNotLoanParticipantTest() throws Exception {
        long loanRequestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanService.getPlannedPaymentsByRequestId(loanRequestId, company)).thenThrow(ForbiddenResourceException.class);

        mockMvc.perform(get(BASE_PATH + "/{loanRequestId}/payments/plan", loanRequestId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpectAll(
                        jsonPath("$.code").value(403),
                        jsonPath("$.status").value("FORBIDDEN"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(loanService, times(1)).getPlannedPaymentsByRequestId(loanRequestId, company);
        verify(plannedPaymentConverter, never()).convert(any());
    }

    @Test
    @DisplayName("Получение плановых платежей по несуществующему кредиту/заявке")
    @WithMockUser
    void getPlannedPaymentsNotFoundLoanRequestTest() throws Exception {
        long loanRequestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanService.getPlannedPaymentsByRequestId(loanRequestId, company)).thenThrow(LoanRequestNotFoundException.class);

        mockMvc.perform(get(BASE_PATH + "/{loanRequestId}/payments/plan", loanRequestId))
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

        verify(loanService, times(1)).getPlannedPaymentsByRequestId(loanRequestId, company);
        verify(plannedPaymentConverter, never()).convert(any());
    }

    @Test
    @DisplayName("Получение плановых платежей")
    @WithMockUser
    void getPlannedPaymentsTest() throws Exception {
        long loanRequestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        PlannedPayment firstPayment = new PlannedPayment();
        PlannedPayment secondPayment = new PlannedPayment();

        PlannedPaymentDto plannedPaymentDto1 = new PlannedPaymentDto(
                BigDecimal.valueOf(100_203.03),
                BigDecimal.valueOf(1233.04),
                LocalDate.of(2022, Month.JUNE, 17)
        );
        PlannedPaymentDto plannedPaymentDto2 = new PlannedPaymentDto(
                BigDecimal.valueOf(2323),
                BigDecimal.valueOf(4334.43),
                LocalDate.of(2022, Month.JULY, 14)
        );

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanService.getPlannedPaymentsByRequestId(loanRequestId, company))
                .thenReturn(List.of(firstPayment, secondPayment));

        when(plannedPaymentConverter.convert(firstPayment)).thenReturn(plannedPaymentDto1);
        when(plannedPaymentConverter.convert(secondPayment)).thenReturn(plannedPaymentDto2);

        mockMvc.perform(get(BASE_PATH + "/{loanRequestId}/payments/plan", loanRequestId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$", hasSize(2)),
                        jsonPath("$[0].principal").value(plannedPaymentDto1.getPrincipal().doubleValue()),
                        jsonPath("$[0].percent").value(plannedPaymentDto1.getPercent().doubleValue()),
                        jsonPath("$[0].date").value("2022-06-17"),
                        jsonPath("$[1].principal").value(plannedPaymentDto2.getPrincipal().doubleValue()),
                        jsonPath("$[1].percent").value(plannedPaymentDto2.getPercent().doubleValue()),
                        jsonPath("$[1].date").value("2022-07-14")
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(loanService, times(1)).getPlannedPaymentsByRequestId(loanRequestId, company);
        verify(plannedPaymentConverter, times(1)).convert(firstPayment);
        verify(plannedPaymentConverter, times(1)).convert(secondPayment);
    }

    @Test
    @DisplayName("Получение фактических платежей без авторизации пользователя")
    void getActualPaymentsWithAccessDeniedTest() throws Exception {
        long requestId = 12L;

        mockMvc.perform(get(BASE_PATH + "/{loanRequestId}/payments/actual", requestId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(blankOrNullString()));

        verify(userService, never()).getCurrentUser(any());
        verify(loanService, never()).getActualPaymentsByRequestId(anyLong(), any());
        verify(actualPaymentConverter, never()).convert(any());
    }

    @Test
    @DisplayName("Получение фактических платежей не участником кредита")
    @WithMockUser
    void getActualPaymentsByNotLoanParticipantTest() throws Exception {
        long loanRequestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanService.getActualPaymentsByRequestId(loanRequestId, company)).thenThrow(ForbiddenResourceException.class);

        mockMvc.perform(get(BASE_PATH + "/{loanRequestId}/payments/actual", loanRequestId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpectAll(
                        jsonPath("$.code").value(403),
                        jsonPath("$.status").value("FORBIDDEN"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(loanService, times(1)).getActualPaymentsByRequestId(loanRequestId, company);
        verify(actualPaymentConverter, never()).convert(any());
    }

    @Test
    @DisplayName("Получение фактических платежей по несуществующему кредиту/заявке")
    @WithMockUser
    void getActualPaymentsNotFoundLoanRequestTest() throws Exception {
        long loanRequestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanService.getActualPaymentsByRequestId(loanRequestId, company)).thenThrow(LoanRequestNotFoundException.class);

        mockMvc.perform(get(BASE_PATH + "/{loanRequestId}/payments/actual", loanRequestId))
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

        verify(loanService, times(1)).getActualPaymentsByRequestId(loanRequestId, company);
        verify(actualPaymentConverter, never()).convert(any());
    }

    @Test
    @DisplayName("Получение фактических платежей")
    @WithMockUser
    void getActualPaymentsTest() throws Exception {
        long loanRequestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        ActualPayment firstPayment = new ActualPayment();
        ActualPayment secondPayment = new ActualPayment();

        ActualPaymentDto actualPaymentDto1 = new ActualPaymentDto(
                100_232,
                LocalDate.of(2022, Month.JUNE, 17)
        );
        ActualPaymentDto actualPaymentDto2 = new ActualPaymentDto(
                13232,
                LocalDate.of(2022, Month.JULY, 14)
        );

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanService.getActualPaymentsByRequestId(loanRequestId, company))
                .thenReturn(List.of(firstPayment, secondPayment));

        when(actualPaymentConverter.convert(firstPayment)).thenReturn(actualPaymentDto1);
        when(actualPaymentConverter.convert(secondPayment)).thenReturn(actualPaymentDto2);

        mockMvc.perform(get(BASE_PATH + "/{loanRequestId}/payments/actual", loanRequestId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$", hasSize(2)),
                        jsonPath("$[0].payment").value(actualPaymentDto1.getPayment()),
                        jsonPath("$[0].date").value("2022-06-17"),
                        jsonPath("$[1].payment").value(actualPaymentDto2.getPayment()),
                        jsonPath("$[1].date").value("2022-07-14")
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(loanService, times(1)).getActualPaymentsByRequestId(loanRequestId, company);
        verify(actualPaymentConverter, times(1)).convert(firstPayment);
        verify(actualPaymentConverter, times(1)).convert(secondPayment);
    }

    @Test
    @DisplayName("Внесение платежа без авторизации пользователя")
    void acceptPaymentWithAccessDeniedTest() throws Exception {
        long requestId = 12L;

        PaymentRequest paymentRequest = new PaymentRequest(
                123_002L,
                LocalDate.of(2022, Month.JUNE, 17)
        );

        mockMvc.perform(
                post(BASE_PATH + "/{loanRequestId}/pay", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, paymentRequest))
        )
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(blankOrNullString()));

        verify(userService, never()).getCurrentUser(any());
        verify(loanService, never()).acceptPayment(anyLong(), any(), any());
        verify(actualPaymentConverter, never()).convert(any());
    }

    @Test
    @DisplayName("Внесение платежа не заемщиком")
    @WithMockUser
    void acceptPaymentByNotBorrowerTest() throws Exception {

        long requestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        PaymentRequest paymentRequest = new PaymentRequest(
                123_002L,
                LocalDate.of(2022, Month.JUNE, 17)
        );

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanService.acceptPayment(requestId, paymentRequest, user)).thenThrow(ForbiddenResourceException.class);


        mockMvc.perform(
                post(BASE_PATH + "/{loanRequestId}/pay", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, paymentRequest))
        )
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

        verify(loanService, times(1)).acceptPayment(requestId, paymentRequest, user);
        verify(actualPaymentConverter, never()).convert(any());
    }

    @ParameterizedTest
    @ValueSource(classes = {
            LoanRequestNotFoundException.class,
            LoanNotFoundException.class,
            NodeNotFoundException.class,
    })
    @DisplayName("Внесение платежа с отсутвием данных")
    @WithMockUser
    void acceptPaymentNotFoundTest(Class<? extends Throwable> exception) throws Exception {

        long requestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        PaymentRequest paymentRequest = new PaymentRequest(
                123_002L,
                LocalDate.of(2022, Month.JUNE, 17)
        );

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanService.acceptPayment(requestId, paymentRequest, user)).thenThrow(exception);


        mockMvc.perform(
                post(BASE_PATH + "/{loanRequestId}/pay", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, paymentRequest))
        )
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

        verify(loanService, times(1)).acceptPayment(requestId, paymentRequest, user);
        verify(actualPaymentConverter, never()).convert(any());
    }

    @Test
    @DisplayName("Внесение платежа с ошибкой сохранения в блокчейн сети")
    @WithMockUser
    void acceptPaymentWithPersistErrorTest() throws Exception {

        long requestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        PaymentRequest paymentRequest = new PaymentRequest(
                123_002L,
                LocalDate.of(2022, Month.JUNE, 17)
        );

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanService.acceptPayment(requestId, paymentRequest, user)).thenThrow(BlockchainPersistException.class);


        mockMvc.perform(
                post(BASE_PATH + "/{loanRequestId}/pay", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, paymentRequest))
        )
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.code").value(500),
                        jsonPath("$.status").value("INTERNAL_SERVER_ERROR"),
                        jsonPath("$.error").exists(),
                        jsonPath("$.message").hasJsonPath(),
                        jsonPath("$.timestamp").exists()
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(loanService, times(1)).acceptPayment(requestId, paymentRequest, user);
        verify(actualPaymentConverter, never()).convert(any());
    }

    @Test
    @DisplayName("Внесение платежа")
    @WithMockUser
    void acceptPaymentTest() throws Exception {

        long requestId = 12L;

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        PaymentRequest paymentRequest = new PaymentRequest(
                123_002L,
                LocalDate.of(2022, Month.JUNE, 17)
        );

        ActualPayment actualPayment = new ActualPayment();

        ActualPaymentDto actualPaymentDto = new ActualPaymentDto(
                paymentRequest.getPayment(),
                paymentRequest.getDate()
        );

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);
        when(loanService.acceptPayment(requestId, paymentRequest, user)).thenReturn(actualPayment);
        when(actualPaymentConverter.convert(actualPayment)).thenReturn(actualPaymentDto);

        mockMvc.perform(
                post(BASE_PATH + "/{loanRequestId}/pay", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, paymentRequest))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.payment").value(paymentRequest.getPayment()),
                        jsonPath("$.date").value("2022-06-17")
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue().getName()).isEqualTo("user");

        verify(loanService, times(1)).acceptPayment(requestId, paymentRequest, user);
        verify(actualPaymentConverter, times(1)).convert(actualPayment);
    }

    @ParameterizedTest
    @MethodSource("notValidPaymentRequest")
    @DisplayName("Внесение платежа с невалидными данными")
    @WithMockUser
    void acceptPaymentWithInvalidRequestTest(PaymentRequest paymentRequest) throws Exception {
        long requestId = 12L;

        mockMvc.perform(
                post(BASE_PATH + "/{loanRequestId}/pay", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(objectMapper, paymentRequest))
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

        verify(loanService, never()).acceptPayment(anyLong(), any(), any());
        verify(actualPaymentConverter, never()).convert(any());
    }

    private static Stream<Arguments> notValidPaymentRequest() {
        return Stream.of(
                Arguments.of(
                        new PaymentRequest(
                                -132,
                                LocalDate.now()
                        )
                ),
                Arguments.of(
                        new PaymentRequest(
                                131,
                                LocalDate.now().plusDays(1)
                        )
                )
        );
    }
}