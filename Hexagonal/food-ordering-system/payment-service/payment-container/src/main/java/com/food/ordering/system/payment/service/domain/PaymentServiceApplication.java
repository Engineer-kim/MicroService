package com.food.ordering.system.payment.service.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// JPA 리포지토리를 활성화하는 어노테이션
@EnableJpaRepositories(basePackages = "com.food.ordering.system.payment.service.dataaccess")
// JPA 엔티티 클래스를 스캔하는 어노테이션
@EntityScan(basePackages = "com.food.ordering.system.payment.service.dataaccess")
// Spring Boot 애플리케이션의 기본 설정을 제공하는 어노테이션
@SpringBootApplication(scanBasePackages = "com.food.ordering.system")
public class PaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}