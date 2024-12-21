package com.assignment.demo.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
public class KafkaMessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaMessagePublisher.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessageToTopic(String message){
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("unique-count", message);

        future.whenComplete((res, ex) -> {
            if(ex==null){
                log.info("Sent message = [{}] with offset=[{}]", message, res.getRecordMetadata().offset());
            } else{
                log.error("Unable to send message {} due to : {}", message, ex.getMessage());
            }
        });
    }
}
