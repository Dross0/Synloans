package com.synloans.loans.configuration;


import com.synloans.loans.configuration.properties.BlockchainServiceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@EnableConfigurationProperties(BlockchainServiceProperties.class)
@Configuration
public class BlockchainServiceConfiguration {

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplateBuilder().build();
    }

}
