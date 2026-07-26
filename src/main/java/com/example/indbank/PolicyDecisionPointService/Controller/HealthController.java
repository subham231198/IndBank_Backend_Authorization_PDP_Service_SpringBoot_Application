package com.example.indbank.PolicyDecisionPointService.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping(value = "/health")
    public ResponseEntity<Map<String, Object>> health() {
        return new ResponseEntity<>(
                 Map.of("status", "UP"),
                HttpStatus.OK
        );
    }
}
