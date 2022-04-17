package com.synloans.loans.service.blockchain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synloans.loans.configuration.properties.BlockchainServiceProperties;
import com.synloans.loans.model.blockchain.BankJoinRequest;
import com.synloans.loans.model.blockchain.LoanCreateRequest;
import com.synloans.loans.model.blockchain.LoanId;
import com.synloans.loans.model.blockchain.PaymentBlockchainRequest;
import com.synloans.loans.model.dto.NodeUserInfo;
import com.synloans.loans.service.blockchain.url.BlockchainServiceUrlFactory;
import com.synloans.loans.service.blockchain.url.BlockchainServiceUrlFactoryImpl;
import com.synloans.loans.service.exception.blockchain.BlockchainPersistException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RestTemplate.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BlockchainServiceImplTest {

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    private ObjectMapper objectMapper;

    private BlockchainServiceUrlFactory blockchainServiceUrlFactory;

    private BlockchainService blockchainService;

    @BeforeAll
    public void setup(){
        objectMapper = new ObjectMapper();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        blockchainServiceUrlFactory = new BlockchainServiceUrlFactoryImpl(
                new BlockchainServiceProperties("http://blockchain-service:9933")
        );
        blockchainService = new BlockchainServiceImpl(blockchainServiceUrlFactory, restTemplate);
    }

    @AfterEach
    public void reset(){
        mockServer.reset();
    }

    @Test
    @DisplayName("Тест. Отправка запроса на создание кредита в блокчейн сервис")
    void sendLoanCreateRequestTest() throws JsonProcessingException {
        LoanId expectedResponse = new LoanId("j1", UUID.randomUUID());

        mockServer.expect(ExpectedCount.once(), requestTo(blockchainServiceUrlFactory.getLoanCreatePostUrl()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(expectedResponse))
                );

        LoanCreateRequest loanCreateRequest = new LoanCreateRequest();

        LoanId loanId = blockchainService.createLoan(loanCreateRequest);
        mockServer.verify();

        assertThat(loanId.getId()).isEqualTo(expectedResponse.getId());
        assertThat(loanId.getLoanExternalId()).isEqualTo(expectedResponse.getLoanExternalId());
    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "NOT_FOUND", "GATEWAY_TIMEOUT"})
    @DisplayName("Тест. Http ошибка от блокчейн сервиса при отправки запроса на создание кредита")
    void sendLoanCreateRequestWithHttpErrorResponseTest(HttpStatus errorStatus) {

        mockServer.expect(ExpectedCount.once(), requestTo(blockchainServiceUrlFactory.getLoanCreatePostUrl()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withStatus(errorStatus)
                );

        LoanCreateRequest loanCreateRequest = new LoanCreateRequest();

        Throwable throwable = catchThrowable(() -> blockchainService.createLoan(loanCreateRequest));
        mockServer.verify();

        assertThat(throwable).isInstanceOf(BlockchainPersistException.class);
    }

    @Test
    @DisplayName("Тест. Отправка запроса на создание кредита в недоступный блокчейн сервис")
    void sendLoanCreateRequestWithoutResourceAccessTest() {

        mockServer.expect(ExpectedCount.once(), requestTo(blockchainServiceUrlFactory.getLoanCreatePostUrl()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(request -> {
                    throw new ResourceAccessException("service dead");
                });

        LoanCreateRequest loanCreateRequest = new LoanCreateRequest();

        Throwable throwable = catchThrowable(() -> blockchainService.createLoan(loanCreateRequest));
        mockServer.verify();

        assertThat(throwable).isInstanceOf(BlockchainPersistException.class);
    }

    @Test
    @DisplayName("Тест. Неизвестная ошибка при отправке запроса на создание кредита в блокчейн сервис")
    void sendLoanCreateRequestWithUnexpectedErrorTest() {

        mockServer.expect(ExpectedCount.once(), requestTo(blockchainServiceUrlFactory.getLoanCreatePostUrl()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(request -> {
                    throw new IllegalStateException();
                });

        LoanCreateRequest loanCreateRequest = new LoanCreateRequest();

        Throwable throwable = catchThrowable(() -> blockchainService.createLoan(loanCreateRequest));
        mockServer.verify();

        assertThat(throwable).isInstanceOf(BlockchainPersistException.class);
    }

    @Test
    @DisplayName("Тест. Отправка запроса на вступление банка в синдикат по кредиту в блокчейн сервис")
    void joinBankRequestTest() {

        mockServer.expect(ExpectedCount.once(), requestTo(blockchainServiceUrlFactory.getBankJoinPostUrl()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withStatus(HttpStatus.OK)
                );

        BankJoinRequest bankJoinRequest = new BankJoinRequest(
                new NodeUserInfo("address", "user", "password"),
                new LoanId("j1", UUID.randomUUID()),
                1000L
        );

        blockchainService.joinBank(bankJoinRequest);
        mockServer.verify();
    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "NOT_FOUND", "GATEWAY_TIMEOUT"})
    @DisplayName("Тест. Http ошибка от блокчейн сервиса при отправки запроса на вступление банка в синдикат")
    void joinBankRequestWithHttpErrorResponseTest(HttpStatus errorStatus) {

        mockServer.expect(ExpectedCount.once(), requestTo(blockchainServiceUrlFactory.getBankJoinPostUrl()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withStatus(errorStatus)
                );

        BankJoinRequest bankJoinRequest = new BankJoinRequest(
                new NodeUserInfo("address", "user", "password"),
                new LoanId("j1", UUID.randomUUID()),
                1000L
        );

        Throwable throwable = catchThrowable(() -> blockchainService.joinBank(bankJoinRequest));
        mockServer.verify();

        assertThat(throwable).isInstanceOf(BlockchainPersistException.class);
    }

    @Test
    @DisplayName("Тест. Отправка запроса на вступление банка в синдикат в недоступный блокчейн сервис")
    void joinBankRequestWithoutResourceAccessTest() {

        mockServer.expect(ExpectedCount.once(), requestTo(blockchainServiceUrlFactory.getBankJoinPostUrl()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(request -> {
                    throw new ResourceAccessException("service dead");
                });

        BankJoinRequest bankJoinRequest = new BankJoinRequest(
                new NodeUserInfo("address", "user", "password"),
                new LoanId("j1", UUID.randomUUID()),
                1000L
        );

        Throwable throwable = catchThrowable(() -> blockchainService.joinBank(bankJoinRequest));
        mockServer.verify();

        assertThat(throwable).isInstanceOf(BlockchainPersistException.class);
    }

    @Test
    @DisplayName("Тест. Неизвестная ошибка при отправке запроса на вступление банка в синдикат в блокчейн сервис")
    void joinBankRequestWithUnexpectedErrorTest() {

        mockServer.expect(ExpectedCount.once(), requestTo(blockchainServiceUrlFactory.getBankJoinPostUrl()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(request -> {
                    throw new IllegalStateException();
                });

        BankJoinRequest bankJoinRequest = new BankJoinRequest(
                new NodeUserInfo("address", "user", "password"),
                new LoanId("j1", UUID.randomUUID()),
                1000L
        );

        Throwable throwable = catchThrowable(() -> blockchainService.joinBank(bankJoinRequest));
        mockServer.verify();

        assertThat(throwable).isInstanceOf(BlockchainPersistException.class);
    }

    @Test
    @DisplayName("Тест. Отправка платежа по кредиту в блокчейн сервис")
    void paymentRequestTest() {

        mockServer.expect(ExpectedCount.once(), requestTo(blockchainServiceUrlFactory.getPaymentPostUrl()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withStatus(HttpStatus.OK)
                );

        PaymentBlockchainRequest paymentBlockchainRequest = new PaymentBlockchainRequest(
                new NodeUserInfo("address", "user", "password"),
                new LoanId("j1", UUID.randomUUID()),
                1000L
        );


        blockchainService.makePayment(paymentBlockchainRequest);
        mockServer.verify();
    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "NOT_FOUND", "GATEWAY_TIMEOUT"})
    @DisplayName("Тест. Http ошибка от блокчейн сервиса при отправке платежа")
    void paymentRequestWithHttpErrorResponseTest(HttpStatus errorStatus) {

        mockServer.expect(ExpectedCount.once(), requestTo(blockchainServiceUrlFactory.getPaymentPostUrl()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withStatus(errorStatus)
                );

        PaymentBlockchainRequest paymentBlockchainRequest = new PaymentBlockchainRequest(
                new NodeUserInfo("address", "user", "password"),
                new LoanId("j1", UUID.randomUUID()),
                1000L
        );

        Throwable throwable = catchThrowable(() -> blockchainService.makePayment(paymentBlockchainRequest));
        mockServer.verify();

        assertThat(throwable).isInstanceOf(BlockchainPersistException.class);
    }

    @Test
    @DisplayName("Тест. Отправка платежа в недоступный блокчейн сервис")
    void paymentRequestWithoutResourceAccessTest() {

        mockServer.expect(ExpectedCount.once(), requestTo(blockchainServiceUrlFactory.getPaymentPostUrl()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(request -> {
                    throw new ResourceAccessException("service dead");
                });

        PaymentBlockchainRequest paymentBlockchainRequest = new PaymentBlockchainRequest(
                new NodeUserInfo("address", "user", "password"),
                new LoanId("j1", UUID.randomUUID()),
                1000L
        );

        Throwable throwable = catchThrowable(() -> blockchainService.makePayment(paymentBlockchainRequest));
        mockServer.verify();

        assertThat(throwable).isInstanceOf(BlockchainPersistException.class);
    }

    @Test
    @DisplayName("Тест. Неизвестная ошибка при отправке платежа в блокчейн сервис")
    void paymentRequestWithUnexpectedErrorTest() {

        mockServer.expect(ExpectedCount.once(), requestTo(blockchainServiceUrlFactory.getPaymentPostUrl()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(request -> {
                    throw new IllegalStateException();
                });

        PaymentBlockchainRequest paymentBlockchainRequest = new PaymentBlockchainRequest(
                new NodeUserInfo("address", "user", "password"),
                new LoanId("j1", UUID.randomUUID()),
                1000L
        );

        Throwable throwable = catchThrowable(() -> blockchainService.makePayment(paymentBlockchainRequest));
        mockServer.verify();

        assertThat(throwable).isInstanceOf(BlockchainPersistException.class);
    }

}