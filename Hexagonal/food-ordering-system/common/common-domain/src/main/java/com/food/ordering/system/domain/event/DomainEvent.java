package com.food.ordering.system.domain.event;

public interface DomainEvent<T>{
    //직접 메세지 발행이 아닌 스케줄러를 통한 발행
   // void fire();
}
