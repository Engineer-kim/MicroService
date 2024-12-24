package com.food.ordering.system.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaMessageHelper {

    public <T> void getKafkaCallBack(SendResult<String, T> result,  String topicName, Throwable exception ,String orderId , String avroModelName) {
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
