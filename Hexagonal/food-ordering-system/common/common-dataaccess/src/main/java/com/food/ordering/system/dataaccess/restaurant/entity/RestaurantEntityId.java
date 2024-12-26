package com.food.ordering.system.dataaccess.restaurant.entity;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class RestaurantEntityId implements Serializable {
    //직렬화 마커 인터페이스 상속 이유
    //단일 키: 단일 키는 하나의 필드(예: id)로 고유하게 식별됩니다.
    //JPA는 단일 키를 관리하기 위해 특별한 직렬화 요구 사항이 없음
    // 단일 키는 일반적으로 기본 데이터 타입이나 간단한 객체로 구성
    //상태 유지: 복합 키를 가진 엔티티는 데이터베이스에 저장될 때, 해당 키의 상태를 유지해야 합니다.
    // 이 상태는 직렬화 과정을 통해 저장되거나 전송될 수 있습니다.
    //영속성 컨텍스트: JPA의 영속성 컨텍스트는 엔티티의 상태를 관리합니다.
    // 복합 키가 포함된 엔티티를 영속화하려면, JPA는 해당 복합 키의 상태를 직렬화하여 관리해야 합니다.
    //성능: 복합 키를 사용하면, JPA는 키의 모든 필드를 포함하여 식별자를 구성해야 합니다.
    // 이를 위해 복합 키를 직렬화하여 데이터베이스와의 상호작용을 최적화할 수 있습니다.
    private UUID restaurantId;
    private UUID productId;

}
