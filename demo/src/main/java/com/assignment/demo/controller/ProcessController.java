package com.assignment.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
@Log4j2
public class ProcessController {

    private final JedisPool jedisPool;

    @GetMapping(value = "/api/verve/accept", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processIdRequest(
            @RequestParam("id") int id, // Compulsory query parameter
            @RequestParam(value = "endpoint", required = false) String endpoint // Optional query parameter
    ) {
        if (id <= 0) {
            return ResponseEntity.badRequest().body("Invalid ID");
        }

        ResponseEntity<String> response = ResponseEntity.ok("ok");

        String redisKey = "unique-requests:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd:HH-mm"));
        try (Jedis jedis = jedisPool.getResource()) {
            // Add the value to the set in Redis
            jedis.sadd(redisKey, String.valueOf(id));
        }

        if (endpoint != null && !endpoint.isEmpty()) {
            sendPostRequestAsync(endpoint);
        }

        log.info("Request processed successfully");
        return response;
    }

    // Asynchronous method to process POST request
    private void sendPostRequestAsync(String endpoint) {
        WebClient webClient = WebClient.create();

        int uniqueReqCount = 0;
        String urlWithCount = endpoint + "?count=" + uniqueReqCount;

        webClient.post()
                .uri(urlWithCount)
                .exchangeToMono(response -> {
                    // Log the HTTP status code
                    System.out.println("HTTP Status Code: " + response.statusCode());
                    return response.bodyToMono(Void.class);
                })
                .subscribe();
    }
}
