package com.assignment.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
@Log4j2
public class ProcessController {

    @Qualifier("reactiveRedisTemplate")
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private final WebClient webClient;


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

        redisTemplate.opsForSet()
                .size(currentMinuteKey)
                .flux()
                .onBackpressureBuffer(1000, size -> log.warn("Request dropped due to overload: {}", size))
                .flatMap(size -> webClient.post()
                        .uri(endpoint + "?count=" + size)
                        .retrieve()
                        .toBodilessEntity()
                        .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)) // Retry up to 3 times with exponential backoff
                                .filter(throwable -> throwable instanceof WebClientResponseException &&
                                        ((WebClientResponseException) throwable).getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE))
                )
                .doOnNext(response -> log.info("HTTP Status: {}", response.getStatusCode()))
                .doOnError(error -> log.error("Error sending POST request: {}", error.getMessage()))
                .subscribe();
    }

}
