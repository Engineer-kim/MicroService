package com.food.ordering.system.kafka.producer.service.impl;

import com.food.ordering.system.kafka.producer.exception.KafkaProducerException;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class KafkaProducerImpl<K extends Serializable, V extends SpecificRecordBase> implements KafkaProducer<K, V> {

   private final KafkaTemplate<K, V> kafkaTemplate;

    public KafkaProducerImpl(KafkaTemplate<K, V> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    @Override
    public CompletableFuture<SendResult<K, V>> send(String topicName, K key, V message) {
        log.info("Sending message={} to topic={}", message, topicName);
        try {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return kafkaTemplate.send(topicName, key, message).get();
                } catch (Exception e) {
                    throw new KafkaProducerException("메시지를 Kafka에 전송하는 데 실패했습니다. 키: " + key + ", 메시지: " + message + ", 오류: " + e.getMessage());
                }
            });
        } catch (Exception ex) {
            log.error("Error on kafka producer with key: {}, message: {}", key, message, ex);
            throw new KafkaProducerException("Kafka 메시지를 처리하는 데 실패했습니다. 오류난 래 이유: " + ex.getMessage());
        }
    }

    @PreDestroy
    public void close() {
        if (kafkaTemplate != null) {
            log.info("Closing kafka producer!");
            kafkaTemplate.destroy();
        }
    }


}
