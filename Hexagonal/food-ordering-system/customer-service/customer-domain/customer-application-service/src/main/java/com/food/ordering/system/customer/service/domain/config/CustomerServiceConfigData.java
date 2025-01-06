package com.food.ordering.system.customer.service.domain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "customer-service")
public class CustomerServiceConfigData {
    //customer-service  > customer-container 내의 application.yml 의  customer-topic-name를 가리킴
    private String customerTopicName;  //메세지 토픽이름(카프카 메세지 발행시 토픽 구분하기 위한 메시지 이름 appl)
}
