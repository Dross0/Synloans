package com.synloans.loans.service.blockchain;

import com.synloans.loans.model.blockchain.BankJoinRequest;
import com.synloans.loans.model.blockchain.LoanCreateRequest;
import com.synloans.loans.model.blockchain.LoanId;
import com.synloans.loans.model.blockchain.PaymentBlockchainRequest;
import com.synloans.loans.service.blockchain.url.BlockchainServiceUrlFactory;
import com.synloans.loans.service.exception.blockchain.BlockchainPersistException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockchainServiceImpl implements BlockchainService {

    private final BlockchainServiceUrlFactory blockchainServiceUrlFactory;

    private final RestTemplate restTemplate;

    @Override
    public LoanId createLoan(LoanCreateRequest loanCreateRequest) {
        URI uri = blockchainServiceUrlFactory.getLoanCreatePostUrl();
        try{
            return restTemplate.postForObject(
                    uri,
                    loanCreateRequest,
                    LoanId.class
            );
        } catch (HttpStatusCodeException e){
            log.error("Http error with code='{}-{}', while create loan with borrower='{}'",
                    e.getStatusCode(),
                    e.getStatusCode().value(),
                    loanCreateRequest.getBorrower()
            );
            throw new BlockchainPersistException("Loan create failed at blockchain", e);
        } catch (ResourceAccessException e){
            log.error("Resource-'{}' is unavailable, while loan create", uri);
            throw new BlockchainPersistException("Resource is unavailable: " + uri, e);
        } catch (Exception e){
            log.error("Unexpected error while creating loan with borrower='{}'", loanCreateRequest.getBorrower());
            throw new BlockchainPersistException("Unexpected error while creating loan", e);
        }
    }

    @Override
    public void joinBank(BankJoinRequest bankJoinRequest) {
        URI uri = blockchainServiceUrlFactory.getBankJoinPostUrl();
        try{
            restTemplate.postForLocation(
                    uri,
                    bankJoinRequest
            );
        } catch (HttpStatusCodeException e){
            log.error("Http error with code='{}-{}', while bank with address='{}' join to loan with id='{}'",
                    e.getStatusCode(),
                    e.getStatusCode().value(),
                    bankJoinRequest.getBank().getAddress(),
                    bankJoinRequest.getLoanId().getId()
            );
            throw new BlockchainPersistException("Bank join failed at blockchain", e);
        } catch (ResourceAccessException e){
            log.error("Resource-'{}' is unavailable, while bank joining", uri);
            throw new BlockchainPersistException("Resource is unavailable: " + uri, e);
        } catch (Exception e){
            log.error("Unexpected error while bank joining to loan with id='{}'", bankJoinRequest.getLoanId().getId());
            throw new BlockchainPersistException("Unexpected error while joining bank", e);
        }
    }

    @Override
    public void makePayment(PaymentBlockchainRequest paymentRequest) {
        URI uri = blockchainServiceUrlFactory.getPaymentPostUrl();
        try{
            restTemplate.postForLocation(
                    uri,
                    paymentRequest
            );
        } catch (HttpStatusCodeException e){
            log.error("Http error with code='{}-{}', while borrower with address='{}' make payment to loan with id='{}'",
                    e.getStatusCode(),
                    e.getStatusCode().value(),
                    paymentRequest.getPayer().getAddress(),
                    paymentRequest.getLoanId().getId()
            );
            throw new BlockchainPersistException("Payment failed at blockchain", e);
        } catch (ResourceAccessException e){
            log.error("Resource-'{}' is unavailable, while make payment", uri);
            throw new BlockchainPersistException("Resource is unavailable: " + uri, e);
        } catch (Exception e){
            log.error("Unexpected error while make payment to loan with id='{}'", paymentRequest.getLoanId().getId());
            throw new BlockchainPersistException("Unexpected error while make payment", e);
        }
    }
}
