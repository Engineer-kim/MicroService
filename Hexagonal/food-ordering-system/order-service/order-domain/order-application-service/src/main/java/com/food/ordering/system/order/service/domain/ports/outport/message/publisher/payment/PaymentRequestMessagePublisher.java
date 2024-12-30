package com.food.ordering.system.order.service.domain.ports.outport.message.publisher.payment;

import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.outbox.OutboxStatus;

import java.util.function.BiConsumer;

public interface PaymentRequestMessagePublisher {

    //BiConsumer
    //두 개의 인자를 받아들이고 아무 값도 반환하지 않는 함수를 표현하는 데 사용
    //사용이유: 단순히 결과에따라 보낼 상태를 완료 혹은 실패로 업데이트 하고(.accept 사용) 싶기때문 (아무것도 리턴하지않고)

    void publish(OrderPaymentOutboxMessage orderPaymentOutboxMessage, BiConsumer<OrderPaymentOutboxMessage, OutboxStatus> outboxCallback);
}