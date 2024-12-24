package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.OrderCreateHelper;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.ports.outport.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class CreateOrderKafkaMessagePublisher implements OrderCreatedPaymentRequestMessagePublisher {
    //클라이언트에서 주문 생성이나 취소 요청이 들어옵니다. 이 요청은 일반적으로 REST API를 통해 이루어집니다.
    //1. 이벤트 발행:
    //요청을 처리한 후, 해당 작업(주문 생성 또는 취소)에 맞는 도메인 이벤트를 발행합니다.
    //예를 들어, 주문 생성 시 OrderCreatedEvent를, 주문 취소 시 OrderCancelledEvent를 생성합니다.
    //2.Avro 모델로 변환:
    //
    //발행된 도메인 이벤트를 Avro 모델로 변환합니다. 이 변환은 카프카에 전송할 수 있는 형식으로 데이터를 준비하는 과정입니다.
    //이 작업은 orderMessagingDataMapper와 같은 매퍼 클래스를 통해 수행됩니다.
    //3 .카프카 주제로 비동기 전송:
    //
    //변환된 메시지를 카프카의 특정 주제에 비동기적으로 전송합니다.
    //이 과정은 kafkaProducer의 send 메서드를 호출하여 이루어집니다.
    //이 메서드는 비동기적으로 동작하며, 메시지를 전송한 후 결과를 CompletableFuture로 반환합니다.
    //전송 결과 처리:
    //
    //전송 결과에 대한 처리를 위해 whenComplete 메서드를 사용하여 콜백을 설정합니다.
    //이 콜백은 메시지 전송이 완료된 후 호출되며, 성공적으로 전송된 경우와 예외가 발생한 경우 각각에 대한 로직을 실행합니다.
    //성공 시에는 메시지의 메타데이터를 로그에 기록하고, 실패 시에는 에러 로그를 기록합니다.
    private final OrderMessagingDataMapper orderMessagingDataMapper;

    private final OrderServiceConfigData orderServiceConfigData;

    private final KafkaProducer<String , PaymentRequestAvroModel> kafkaProducer;
    private final KafkaMessageHelper kafkaMessageHelper;

    public CreateOrderKafkaMessagePublisher(OrderMessagingDataMapper orderMessagingDataMapper, OrderServiceConfigData orderServiceConfigData, KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer, KafkaMessageHelper kafkaMessageHelper, OrderCreateHelper orderCreateHelper) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaProducer = kafkaProducer;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }


    @Override
    public void publish(OrderCreatedEvent domainEvent) {
        String orderId = domainEvent.getOrder().getId().getValue().toString();

        PaymentRequestAvroModel paymentRequestAvroModel = orderMessagingDataMapper
                .orderPaymentEventToPaymentRequestAvroModel(domainEvent);

        // Kafka 메시지 전송
        CompletableFuture<SendResult<String, PaymentRequestAvroModel>> future =
                kafkaProducer.send(
                    orderServiceConfigData.getPaymentRequestTopicName(),
                    orderId,
                    paymentRequestAvroModel
        );

        // 전송 결과 처리
        future.whenComplete((result, exception) ->
                kafkaMessageHelper.getKafkaCallBack(result, orderServiceConfigData.getPaymentRequestTopicName(),exception, orderId , "")
        );
    }
}
