package com.assignment.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcessController {

    @GetMapping(value = "/api/verve/accept", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processIdRequest(
            @RequestParam("id") int id, // Compulsory query parameter
            @RequestParam(value = "endpoint", required = false) String endpoint // Optional query parameter
    ) {
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }
}
