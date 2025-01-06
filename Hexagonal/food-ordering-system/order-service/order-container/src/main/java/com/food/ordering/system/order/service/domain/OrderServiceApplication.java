                                                                                                    package com.food.ordering.system.order.service.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = { "com.food.ordering.system.order.service.dataaccess", "com.food.ordering.system.dataaccess" })
//엔티티 스캔
@EntityScan(basePackages = { "com.food.ordering.system.order.service.dataaccess", "com.food.ordering.system.dataaccess"})
//지정된 패키지에서 Spring 컴포넌트를 검색합니다. 여기서는 com.food.ordering.system 패키지를 스캔
@SpringBootApplication(scanBasePackages = "com.food.ordering.system")
public class OrderServiceApplication {
    public static void main(String[] args) {
      SpringApplication.run(OrderServiceApplication.class, args);
    }
}
