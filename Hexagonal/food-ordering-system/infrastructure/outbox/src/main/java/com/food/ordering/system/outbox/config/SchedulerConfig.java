package com.food.ordering.system.outbox.config;

import lombok.EqualsAndHashCode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
// EnableScheduling 스프링 애플리케이션 내에서 특정 작업을 정해진 시간 또는 주기에 따라 자동으로 실행
public class SchedulerConfig {
//    @Bean
//    @Primary
//    public ObjectMapper objectMapper() {
//        return new ObjectMapper()
//                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) // 알 수 없는 속성이 있을 경우 예외를 발생시키지 않도록 설정
//                .registerModule(new JavaTimeModule());// Java 8의 날짜 및 시간 API를 지원하기 위한 모듈 등록
//    }

}
