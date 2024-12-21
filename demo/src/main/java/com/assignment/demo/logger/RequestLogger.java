package com.assignment.demo.logger;

import com.assignment.demo.service.KafkaMessagePublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class RequestLogger {

    @Qualifier("redisTemplate")
    private final RedisTemplate<String, String> redisTemplate;

    private final KafkaMessagePublisher kafkaMessagePublisher;

    private static final Logger logger = LoggerFactory.getLogger(RequestLogger.class);

    @Scheduled(cron = "0 * * * * *") // Runs at the top of every minute
    public void logUniqueRequestCount() {
        String currentMinuteKey = "unique-requests:" + LocalDateTime.now().minusMinutes(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd:HH-mm"));
        String prevMinuteWindow = LocalDateTime.now().minusMinutes(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd:HH-mm")) + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd:HH-mm"));

        Long uniqueCount = redisTemplate.opsForSet().size(currentMinuteKey);

        if (uniqueCount != null) {
            redisTemplate.delete(currentMinuteKey);  // Synchronous delete
            String message = "Unique requests received in the last minute " + prevMinuteWindow + " : " + uniqueCount;
            logger.info("Unique requests received in the last minute {}: {}", prevMinuteWindow, uniqueCount);
            kafkaMessagePublisher.sendMessageToTopic(message);
        }

    }
}
