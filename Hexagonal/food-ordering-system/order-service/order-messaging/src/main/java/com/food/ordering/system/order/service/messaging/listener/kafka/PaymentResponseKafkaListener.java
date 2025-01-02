package com.food.ordering.system.order.service.messaging.listener.kafka;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentStatus;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.payment.PaymentResponseMessageListener;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class PaymentResponseKafkaListener implements KafkaConsumer<PaymentResponseAvroModel> {

    private final PaymentResponseMessageListener paymentResponseMessageListener;
    private final OrderMessagingDataMapper orderMessagingDataMapper;

    public PaymentResponseKafkaListener(PaymentResponseMessageListener paymentResponseMessageListener, OrderMessagingDataMapper orderMessagingDataMapper) {
        this.paymentResponseMessageListener = paymentResponseMessageListener;
        this.orderMessagingDataMapper = orderMessagingDataMapper;
    }


    @Override
    @KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}" ,
            topics = "${order-service.customer-topic-name}")
    public void receive(@Payload List<PaymentResponseAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID)  List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        messages.forEach(paymentResponseAvroModel ->{
            try {
                if (PaymentStatus.COMPLETED == paymentResponseAvroModel.getPaymentStatus()) {
                    log.info("Processing successful payment for order id: {}", paymentResponseAvroModel.getOrderId());
                    paymentResponseMessageListener.paymentCompleted(orderMessagingDataMapper.paymentResponseAvroModelToPaymentResponse(paymentResponseAvroModel));
                } else if (PaymentStatus.CANCELLED == paymentResponseAvroModel.getPaymentStatus() ||
                        PaymentStatus.FAILED == paymentResponseAvroModel.getPaymentStatus()) {
                    log.info("Processing unsuccessful payment for order id: {}", paymentResponseAvroModel.getOrderId());
                    paymentResponseMessageListener.paymentCancelled(orderMessagingDataMapper.paymentResponseAvroModelToPaymentResponse(paymentResponseAvroModel));
                }
                //바로 밑의 익셉션인 낙관적관련 오류는
                //예를 들어, 두 개의 스레드가 동시에 같은 주문을 수정하려고 할 때, 
                // 첫 번째 스레드가 주문을 수정하고 커밋하면 해당 주문의 버전이 업데이트됨
                // 이후 두 번째 스레드가 같은 주문을 수정하려고 하면, 데이터베이스는 두 번째 스레드의 수정 요청을 거부하고 OptimisticLockingFailureException을 발생
            } catch (OptimisticLockingFailureException e) {
                // 동시에 같은 데이터를 수정하려는 두 개 이상의 트랜잭션이 있어야 하며, 후속 트랜잭션이 이전 트랜잭션에 의해 이미 수정된 데이터를 수정하려고 시도할 때 발생
                log.error("Caught optimistic locking exception in PaymentResponseKafkaListener for order id: {}",
                        paymentResponseAvroModel.getOrderId());
            } catch (OrderNotFoundException e) {
                log.error("No order found for order id: {}", paymentResponseAvroModel.getOrderId());
            }
        });
    }
}
