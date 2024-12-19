package com.assignment.demo.logger;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class RequestLogger {

    private final JedisPool jedisPool;

    private static final Logger logger = LoggerFactory.getLogger(RequestLogger.class);

    @Scheduled(cron = "0 * * * * *") // Runs at the top of every minute
    public void logUniqueRequestCount() {
        String currentMinuteKey = "unique-requests:" + LocalDateTime.now().minusMinutes(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd:HH-mm"));
        String prevMinuteWindow = LocalDateTime.now().minusMinutes(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd:HH-mm")) + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd:HH-mm"));

        try (Jedis jedis = jedisPool.getResource()) {
            // Get the size of the set
            long uniqueCount = jedis.scard(currentMinuteKey);

            // Delete the set (key) from Redis
            jedis.del(currentMinuteKey);

            logger.info("Unique requests received in the last minute {} : {}", prevMinuteWindow, uniqueCount);
        }

    }
}
