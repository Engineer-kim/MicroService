package com.food.ordering.system.order.service.dataaccess.order.repository;

import com.food.ordering.system.order.service.dataaccess.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {
    //프록시 생성: OrderRepository 인터페이스를 상속받는 순간 Spring이 프록시 객체를 생성
    //클라이언트 호출: 클라이언트 코드에서 OrderRepository의 메서드를 호출하면, 프록시 객체가 대신 처리.
    //EntityManager 위임: 프록시가 EntityManager에 요청을 위임하여 데이터베이스와 상호작용합니다.
    //결과 반환: EntityManager가 반환한 결과를 클라이언트 코드에 전달합니다.

    Optional<OrderEntity> findByTrackingId(UUID trackingId);


}
