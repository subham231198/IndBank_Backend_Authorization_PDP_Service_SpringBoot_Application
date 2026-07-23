package com.example.indbank.PolicyDecisionPointService.Utility;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RestTemplateUtility {

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> sendPostRequest(
            String url,
            Map<String, Object> body,
            Map<String, Object> headersMap
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headersMap.forEach((key, value) -> headers.set(key, value.toString()));

        HttpEntity<Map> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> result = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
        );
        Integer statusCode = result.getStatusCode().value();
        Map<String, Object> responseBody = new ConcurrentHashMap<>();
        responseBody.put("statusCode", statusCode);
        responseBody.put("headers", result.getHeaders());
        responseBody.put("body", result.getBody());
        return responseBody;
    }

    public Map<String, Object> sendGetRequest(
            String url,
            String body,
            Map<String, Object> headersMap
    ) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headersMap.forEach((key, value) -> headers.set(key, value.toString()));

        HttpEntity<String> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> result = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                Map.class
        );
        Integer statusCode = result.getStatusCode().value();
        Map<String, Object> responseBody = new ConcurrentHashMap<>();
        responseBody.put("statusCode", statusCode);
        responseBody.put("headers", result.getHeaders());
        responseBody.put("body", result.getBody());
        return responseBody;
    }

    public Map<String, Object> sendPutRequest(
            String url,
            String body,
            Map<String, Object> headersMap
    ) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headersMap.forEach((key, value) -> headers.set(key, value.toString()));

        HttpEntity<String> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> result = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                request,
                Map.class
        );
        Integer statusCode = result.getStatusCode().value();
        Map<String, Object> responseBody = new ConcurrentHashMap<>();
        responseBody.put("statusCode", statusCode);
        responseBody.put("headers", result.getHeaders());
        responseBody.put("body", result.getBody());
        return responseBody;
    }

    public Map<String, Object> sendDeleteRequest(
            String url,
            String body,
            Map<String, Object> headersMap
    ) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headersMap.forEach((key, value) -> headers.set(key, value.toString()));

        HttpEntity<String> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> result = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                request,
                Map.class
        );
        Integer statusCode = result.getStatusCode().value();
        Map<String, Object> responseBody = new ConcurrentHashMap<>();
        responseBody.put("statusCode", statusCode);
        responseBody.put("headers", result.getHeaders());
        responseBody.put("body", result.getBody());
        return responseBody;
    }
}