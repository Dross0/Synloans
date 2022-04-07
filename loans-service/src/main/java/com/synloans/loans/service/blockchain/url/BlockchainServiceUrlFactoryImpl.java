package com.synloans.loans.service.blockchain.url;

import com.synloans.loans.configuration.properties.BlockchainServiceProperties;
import com.synloans.loans.configuration.url.BlockchainServiceUrls;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;

import java.net.URI;

@Service
public class BlockchainServiceUrlFactoryImpl implements BlockchainServiceUrlFactory {

    private final UriBuilderFactory uriBuilderFactory;


    public BlockchainServiceUrlFactoryImpl(BlockchainServiceProperties blockchainServiceProperties){
        uriBuilderFactory = new DefaultUriBuilderFactory(blockchainServiceProperties.getHost());
    }

    @Override
    public URI getLoanCreatePostUrl(){
        return uriBuilderFactory.builder()
                .path(BlockchainServiceUrls.LOAN_CREATE_PATH)
                .build();
    }

    @Override
    public URI getBankJoinPostUrl(){
        return uriBuilderFactory.builder()
                .path(BlockchainServiceUrls.BANK_JOIN_PATH)
                .build();
    }

    @Override
    public URI getPaymentPostUrl(){
        return uriBuilderFactory.builder()
                .path(BlockchainServiceUrls.MAKE_PAYMENT_PATH)
                .build();
    }



}
