package com.example.indbank.PolicyDecisionPointService.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/api")
public class OAuth2AccessTokenController {

    @Value("${app.host.authentication.service}")
    private String authentication_service_host;

    @Value("${app.url.authentication.service.oauth_token}")
    private String access_token;

    @Value("${app.url.authentication.service.oauth_introspect}")
    private String introspect;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping(
            value = "/v1/oauth/access_token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> getAccessToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("code") String code,
            @RequestParam("redirect_uri") String redirectURL,
            @RequestHeader("Authorization") String authorization
    ) {

        if (code == null || code.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "invalid_request",
                            "error_description", "code is required"));
        }

        if (redirectURL == null || redirectURL.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "invalid_request",
                            "error_description", "redirect_uri is required"));
        }

        if (authorization == null || authorization.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid_client",
                            "error_description", "Authorization header required"));
        }

        if (!authorization.startsWith("Basic ")) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid_client",
                            "error_description", "Only Basic authentication supported"));
        }

        try {
            String url = authentication_service_host + access_token;

            String tokenUrl = UriComponentsBuilder
                    .fromUriString(url)
                    .queryParam("code", code)
                    .queryParam("redirect_uri", redirectURL)
                    .queryParam("grant_type", grantType)
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", authorization);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            log.info("Token Response Status: {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                String accessToken = (String) responseBody.get("access_token");
                if (accessToken != null) {
                    return ResponseEntity.ok(responseBody);
                }
            }

            return ResponseEntity
                    .status(response.getStatusCode())
                    .body(response.getBody());

        } catch (HttpClientErrorException ex) {
            log.error("Token exchange failed: {}", ex.getMessage());
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode error = mapper.readTree(ex.getResponseBodyAsString());
                return ResponseEntity
                        .status(ex.getStatusCode())
                        .body(error);
            } catch (Exception e) {
                return ResponseEntity
                        .status(ex.getStatusCode())
                        .body(Map.of("error", "server_error",
                                "error_description", ex.getMessage()));
            }
        } catch (Exception e) {
            log.error("Failed to get access token", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "server_error",
                            "error_description", e.getMessage()));
        }
    }
}
