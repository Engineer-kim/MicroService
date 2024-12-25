package com.food.ordering.system.payment.service.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public PaymentDomainService paymentDomainService() { //빈의 타입은 paymentDomainService 인스턴스는 그 구현체인  PaymentDomainServiceImpl
        return new PaymentDomainServiceImpl();
    }
}