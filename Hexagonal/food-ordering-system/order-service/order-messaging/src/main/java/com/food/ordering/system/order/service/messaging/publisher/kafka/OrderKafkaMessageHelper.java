package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderKafkaMessageHelper {

    public <T> void getKafkaCallBack(SendResult<String, T> result, Throwable exception ,String orderId) {
        if (exception != null) {
            // 실패 처리
            log.error("Error sending message to Kafka: {}", exception.getMessage());
        } else {
            //성공일떄는
            RecordMetadata metadata = result.getRecordMetadata();
            log.info("Message sent to Kafka successfully for OrderId : {},  Topic: {}, Partition: {}, Offset: {}",
                    orderId,
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset());
        }
    }
}
