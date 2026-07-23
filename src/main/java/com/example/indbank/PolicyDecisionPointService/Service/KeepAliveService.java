package com.example.indbank.PolicyDecisionPointService.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class KeepAliveService {

    @Autowired
    private SessionAttributesService sessionAttributesService;

    public ResponseEntity<?> keepAlive(String tokenId, String channel) {

        Map<String, Object> response = sessionAttributesService.SessionAttributes(tokenId, channel);

        if (!"200".equals(String.valueOf(response.get("statusCode"))))
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);

        Map<String, Object> body = (Map<String, Object>) response.get("body");
        if ("{}".equals(body))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid Session"));

        boolean isSessionValid =
                Boolean.parseBoolean(body.get("isSessionValid").toString());

        LinkedHashMap<String, Object> output = new LinkedHashMap<>();
        output.put("isSessionValid", isSessionValid);

        if (isSessionValid)
            output.put("correlationId", body.get("correlationId"));

        return ResponseEntity.ok(output);
    }
}
