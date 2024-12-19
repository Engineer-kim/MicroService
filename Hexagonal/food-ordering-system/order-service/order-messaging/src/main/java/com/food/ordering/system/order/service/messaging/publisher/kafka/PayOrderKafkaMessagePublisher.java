package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.ports.outport.message.publisher.restaurantapproval.OrderPaidRestaurantRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class PayOrderKafkaMessagePublisher implements OrderPaidRestaurantRequestMessagePublisher {
    private final OrderMessagingDataMapper orderMessagingDataMapper;

    private final OrderServiceConfigData orderServiceConfigData;


    private final KafkaProducer<String , RestaurantApprovalRequestAvroModel> kafkaProducer;

    private final OrderKafkaMessageHelper orderKafkaMessageHelper;

    public PayOrderKafkaMessagePublisher(OrderMessagingDataMapper orderMessagingDataMapper, OrderServiceConfigData orderServiceConfigData, KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer, OrderKafkaMessageHelper orderKafkaMessageHelper) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaProducer = kafkaProducer;
        this.orderKafkaMessageHelper = orderKafkaMessageHelper;
    }

    @Override
    public void publish(OrderPaidEvent domainEvent) {
        String orderId = domainEvent.getOrder().getId().getValue().toString();

        RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel = orderMessagingDataMapper
                .orderPaidEventToRestaurantApprovalRequestAvroModel(domainEvent);

        // Kafka 메시지 전송
        CompletableFuture<SendResult<String, RestaurantApprovalRequestAvroModel>> future =
                kafkaProducer.send(
                        orderServiceConfigData.getPaymentRequestTopicName(),
                        orderId,
                        restaurantApprovalRequestAvroModel
                );

        // 전송 결과 처리
        future.whenComplete((result, exception) ->
                orderKafkaMessageHelper.getKafkaCallBack(result, exception, orderId)
        );
    }
}
