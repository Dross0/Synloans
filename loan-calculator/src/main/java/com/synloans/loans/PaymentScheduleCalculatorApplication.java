package com.synloans.loans;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class PaymentScheduleCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentScheduleCalculatorApplication.class, args);
    }

}
