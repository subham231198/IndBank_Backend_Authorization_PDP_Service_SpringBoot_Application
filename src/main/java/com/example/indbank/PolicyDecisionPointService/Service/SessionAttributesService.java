package com.example.indbank.PolicyDecisionPointService.Service;

import com.example.indbank.PolicyDecisionPointService.Utility.RestTemplateUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SessionAttributesService {

    @Value("${app.host.authentication.service}")
    private String authenticationHost;

    @Value("${app.url.authentication.service.session_attributes}")
    private String sessionAttributesURL;

    @Autowired
    private RestTemplateUtility restTemplateUtility;

    public ResponseEntity<?> getSessionInfo(String tokenId, String channel) {
        Map<String, Object> response = SessionAttributes(tokenId, channel);
        if (!"200".equals(String.valueOf(response.get("statusCode"))))
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);

        Map<String, Object> body = (Map<String, Object>) response.get("body");
        if ("{}".equals(body))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid Session"));

        boolean isSessionValid =
                Boolean.parseBoolean(body.get("isSessionValid").toString());

        LinkedHashMap<String, Object> output = new LinkedHashMap<>();

        if (isSessionValid) {
            output.put("isSessionValid", isSessionValid);
            output.put("correlationId", body.get("correlationId"));
            output.put("auth_level", body.get("authLevel"));
            output.put("maxIdleExpirationTime", body.get("maxIdleExpirationTime"));
            output.put("maxSessionExpirationTime", body.get("expiresAt"));
            return ResponseEntity.ok(output);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
        }
    }

    public ResponseEntity<?> getAllSessionInfo(String tokenId, String channel) {
        Map<String, Object> response = SessionAttributes(tokenId, channel);
        if (!"200".equals(String.valueOf(response.get("statusCode"))))
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);

        Map<String, Object> body = (Map<String, Object>) response.get("body");
        if ("{}".equals(body))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid Session"));

        boolean isSessionValid =
                Boolean.parseBoolean(body.get("isSessionValid").toString());

        LinkedHashMap<String, Object> output = new LinkedHashMap<>();

        if (isSessionValid) {
            output.put("isSessionValid", isSessionValid);
            output.put("customerId", body.get("customerId"));
            output.put("correlationId", body.get("correlationId"));
            output.put("auth_level", body.get("authLevel"));
            output.put("channel", body.get("channel"));
            output.put("maxIdleExpirationTime", body.get("maxIdleExpirationTime"));
            output.put("maxSessionExpirationTime", body.get("expiresAt"));
            output.put("serviceId", body.get("serviceId"));
            return ResponseEntity.ok(output);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
        }
    }

    public Map<String, Object> SessionAttributes(String tokenId, String channel) {
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        headers.put("customerSessionId", tokenId);
        headers.put("X-Channel", channel);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("tokenId", tokenId);

        String url = authenticationHost + sessionAttributesURL;

        return restTemplateUtility.sendPostRequest(url, request, headers);
    }
}
