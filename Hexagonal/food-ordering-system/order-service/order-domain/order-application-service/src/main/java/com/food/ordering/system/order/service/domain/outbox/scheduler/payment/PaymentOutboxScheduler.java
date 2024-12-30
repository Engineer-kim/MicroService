package com.food.ordering.system.order.service.domain.outbox.scheduler.payment;

import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.outport.message.publisher.payment.PaymentRequestMessagePublisher;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PaymentOutboxScheduler implements OutboxScheduler {

    //주기적으로 아웃박스 메시지를 처리하는 역할
    //카프카의 응답 시간이나 메시지 전송의 성공 여부에 따라 processOutboxMessage 메서드가 호출되지 않거나, 호출되더라도 메시지가 전송되지 않을 수 있음
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final PaymentRequestMessagePublisher paymentRequestMessagePublisher;

    public PaymentOutboxScheduler(PaymentOutboxHelper paymentOutboxHelper,PaymentRequestMessagePublisher paymentRequestMessagePublisher) {
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.paymentRequestMessagePublisher = paymentRequestMessagePublisher;
    }
    @Override
    @Transactional
    @Scheduled(fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}", //메서드 실행 후 다음 실행까지의 지연 시간.
            initialDelayString = "${order-service.outbox-scheduler-initial-delay}") //애플리케이션 시작 후 첫 번째 실행까지의 지연 시간.
    public void processOutboxMessage() {
        Optional<List<OrderPaymentOutboxMessage>> outboxMessagesResponse = paymentOutboxHelper.getPaymentOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus.STARTED, SagaStatus.STARTED, SagaStatus.COMPENSATING);
        if (outboxMessagesResponse.isPresent() && !outboxMessagesResponse.get().isEmpty()) {
            List<OrderPaymentOutboxMessage> outboxMessages = outboxMessagesResponse.get();
            log.info("Received {} OrderPaymentOutboxMessage with ids: {}, sending to message bus!", outboxMessages.size(), outboxMessages.stream().map(outboxMessage -> outboxMessage.getId().toString()).collect(Collectors.joining(",")));
            outboxMessages.forEach(outboxMessage -> paymentRequestMessagePublisher.publish(outboxMessage, this::updateOutboxStatus));
            log.info("{} OrderPaymentOutboxMessage sent to message bus!", outboxMessages.size());
        }

    }

    private void updateOutboxStatus(OrderPaymentOutboxMessage orderPaymentOutboxMessage, OutboxStatus outboxStatus) {
        orderPaymentOutboxMessage.setOutboxStatus(outboxStatus);
        paymentOutboxHelper.save(orderPaymentOutboxMessage);
        log.info("OrderPaymentOutboxMessage is updated with outbox status: {}", outboxStatus.name());
    }

    //특징	사가 패턴 (Saga Pattern) |	아웃박스 패턴 (Outbox Pattern)
    //목적	장기 실행 트랜잭션의 일관성 | 관리	메시지의 안정적인 발행
    //초점	비즈니스 로직 흐름, 보상 트랜잭션 | 메시지 전달의 신뢰성
    //구현 방식	오케스트레이션, 코레오그래피 | Outbox 테이블 사용
    //사용 사례	여러 서비스에 걸친 복잡한 비즈니스 프로세스 (주문, 결제 등) | 데이터베이스 변경과 메시지 발행을 원자적으로 묶어야 할 때
}