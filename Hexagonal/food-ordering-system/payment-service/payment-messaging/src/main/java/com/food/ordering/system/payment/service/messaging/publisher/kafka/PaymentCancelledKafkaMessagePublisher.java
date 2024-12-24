package com.food.ordering.system.payment.service.messaging.publisher.kafka;

import com.food.ordering.system.domain.config.PaymentServiceConfigData;
import com.food.ordering.system.domain.ports.output.message.publisher.PaymentCancelledMessagePublisher;
import com.food.ordering.system.domain.ports.output.message.publisher.PaymentCompletedMessagePublisher;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class PaymentCancelledKafkaMessagePublisher implements PaymentCancelledMessagePublisher {

    private final PaymentMessagingDataMapper paymentMessagingDataMapper;
    private final KafkaProducer<String, PaymentResponseAvroModel> kafkaProducer;
    private final PaymentServiceConfigData paymentServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;

    public PaymentCancelledKafkaMessagePublisher(PaymentMessagingDataMapper paymentMessagingDataMapper, KafkaProducer<String, PaymentResponseAvroModel> kafkaProducer, PaymentServiceConfigData paymentServiceConfigData, KafkaMessageHelper kafkaMessageHelper) {
        this.paymentMessagingDataMapper = paymentMessagingDataMapper;
        this.kafkaProducer = kafkaProducer;
        this.paymentServiceConfigData = paymentServiceConfigData;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }


    @Override
    public void publish(PaymentCancelledEvent domainEvent) {

        String orderId = domainEvent.getPayment().getOrderId().getValue().toString();
        log.info("Received PaymentCancelledEvent for order id: {}", orderId );
        PaymentResponseAvroModel paymentResponseAvroModel = paymentMessagingDataMapper.paymentCancelledEventToPaymentResponseAvroModel(domainEvent);
        // Kafka 메시지 전송
        CompletableFuture<SendResult<String, PaymentResponseAvroModel>> future =
                kafkaProducer.send(
                        paymentServiceConfigData.getPaymentRequestTopicName(),
                        orderId,
                        paymentResponseAvroModel
                );

        // 전송 결과 처리
        future.whenComplete((result, exception) ->
                kafkaMessageHelper.getKafkaCallBack(result ,paymentServiceConfigData.getPaymentRequestTopicName() ,exception, orderId, ""));

        log.info("PaymentResponseAvroModel sent to kafka for order id: {} and saga id: {}", paymentResponseAvroModel.getOrderId(), "");
    }
}
