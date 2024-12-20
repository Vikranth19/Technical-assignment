package com.assignment.demo.controller;

import com.assignment.demo.PostRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Log4j2
public class ProcessController {

    @Qualifier("reactiveRedisTemplate")
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private final PostRequestService postRequestService;


    @GetMapping(value = "/api/verve/accept", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> processIdRequest(
            @RequestParam("id") int id, // Compulsory query parameter
            @RequestParam(value = "endpoint", required = false) String endpoint // Optional query parameter
    ) {
        if (id <= 0) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid ID"));
        }


        String redisKey = "unique-requests:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd:HH-mm"));

        return redisTemplate.opsForSet()
                .add(redisKey, String.valueOf(id))
                .then(redisTemplate.expire(redisKey, Duration.ofSeconds(65))) // wait for both add and expire to finish
                .doOnTerminate(() -> {
                    if (endpoint != null && !endpoint.isEmpty()) {
                        sendPostRequestAsync(endpoint, redisKey);
                    }
                })
                .then(Mono.just(ResponseEntity.ok("ok"))) // Return the response after all async operations are done
                .doOnError(error -> log.error("Error processing request: {}", error.getMessage()));
    }

    // Asynchronous method to process POST request
    private void sendPostRequestAsync(String endpoint, String currentMinuteKey) {
        // Fetch the members of the Redis set using opsForSet
        postRequestService.sendPostRequestAsync(endpoint, 9L);
    }
}
