package com.example.indbank.PolicyDecisionPointService.Service;

import com.example.indbank.PolicyDecisionPointService.Exceptions.LogOffProviderException;
import com.example.indbank.PolicyDecisionPointService.Utility.RestTemplateUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class LogOffService {

    @Value("${app.host.authentication.service}")
    private String authenticationHost;

    @Value("${app.url.authentication.service.logoff}")
    private String logOffURL;

    @Autowired
    private RestTemplateUtility restTemplateUtility;

    public ResponseEntity<?> logOff(String tokenId) {

        try {
            String url = authenticationHost + logOffURL;

            Map<String, Object> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("X-CustomerSessionId", tokenId);

            Map<String, Object> response = restTemplateUtility.sendPostRequest(
                    url,
                    null,
                    headers
            );

            HttpStatus status = HttpStatus.valueOf(
                    Integer.parseInt(response.get("statusCode").toString())
            );

            Map<String, Object> responseBody = (Map<String, Object>) response.get("body");

            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        }
        catch (HttpClientErrorException e) {
            throw new LogOffProviderException("Invalid customer session ID");
        }
    }
}