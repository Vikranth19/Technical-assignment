package com.assignment.demo;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@Service
@RequiredArgsConstructor
public class PostRequestService {

    private static final Logger log = LoggerFactory.getLogger(PostRequestService.class);
    private final RestTemplate restTemplate;
    private final ThreadPoolTaskExecutor taskExecutor;

    // Asynchronous method to send a POST request
    @Async
    public void sendPostRequestAsync(String endpoint, Long size) {
        String url = UriComponentsBuilder.fromUriString(endpoint)
                .queryParam("count", size) // Add query parameter "count"
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Create a task to send POST request asynchronously
        taskExecutor.execute(() -> {
            try {
                // Send the request asynchronously using RestTemplate
                ResponseEntity<String> response = restTemplate.exchange(
                        url, HttpMethod.POST, entity, String.class);

                // Log the response status or handle the response accordingly
                log.info("Response code - {}", response.getStatusCode());
            } catch (Exception e) {
                // Handle exception
                System.err.println("Error sending POST request: " + e.getMessage());
            }
        });
    }
}
