package com.food.ordering.system.domain.event;


//트랜잭션 실패시 롤백하기 위한 빈 클래스
public final class EmptyEvent implements DomainEvent<Void>{

    public  static final EmptyEvent INSTANCE = new EmptyEvent();

    private EmptyEvent(){

    }
}
