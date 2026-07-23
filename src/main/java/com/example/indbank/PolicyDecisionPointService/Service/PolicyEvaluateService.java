package com.example.indbank.PolicyDecisionPointService.Service;

import com.example.indbank.PolicyDecisionPointService.DTO.PolicyEvaluateResponseDTO;
import com.example.indbank.PolicyDecisionPointService.Exceptions.AccessDeniedException;
import com.example.indbank.PolicyDecisionPointService.Utility.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Slf4j
@Service
public class PolicyEvaluateService {

    @Autowired
    private SessionAttributesService sessionAttributesService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.host.authentication.service}")
    private String authHost;

    @Value("${app.url.authentication.service.oauth_introspect}")
    private String introspectPath;

    @Value("${oauth.pdp.clientId}")
    private String clientId;

    @Value("${oauth.pdp.clientSecret}")
    private String clientSecret;

    private static final String BASIC = "Basic ";
    private static final String AUTH_HEADER = "Authorization";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String FORM_URLENCODED = "application/x-www-form-urlencoded";

    public ResponseEntity<?> createSSOTokenToJWT(String ssoToken, String channel) {
        Map<String, Object> session = sessionAttributesService.SessionAttributes(ssoToken, channel);
        validateSessionResponse(session);

        Map<String, Object> body = getSessionBody(session);
        validateSessionBody(body);

        String customerId = body.get("customerId").toString();
        String customerServiceId = body.get("serviceId").toString();
        String correlationId = body.get("correlationId").toString();
        String authLevel = body.get("authLevel").toString();

        String customerJwt = jwtUtil.generate_policyCustomerJwt(
                customerId,
                customerServiceId,
                channel,
                "read",
                authLevel,
                "http://localhost:5059"
        );

        Map<String, Object> actions = new HashMap<>();
        actions.put("POST", true);

        PolicyEvaluateResponseDTO responseDTO = new PolicyEvaluateResponseDTO();
        responseDTO.setResource("APIResource");
        responseDTO.setActions(actions);
        responseDTO.setApplication("AuthorizationPolicyDecisionService");
        responseDTO.setCorrelationId(List.of(correlationId));
        responseDTO.setIssuedToken(List.of(customerJwt));

        return ResponseEntity.ok(responseDTO);
    }

    public ResponseEntity<?> createAccessTokenToCustomerJwt(String token) {
        Map<String, Object> introspectResponse = introspectToken(token);
        validateActiveToken(introspectResponse);

        Map<String, Object> session = sessionAttributesService.SessionAttributes(
                introspectResponse.get("customerSessionId").toString(),
                introspectResponse.get("channel").toString()
        );
        validateSessionResponse(session);

        Map<String, Object> sessionBody = getSessionBody(session);
        validateSessionBody(sessionBody);

        String customerJwt = jwtUtil.generateOAuthCustomerJwt(
                introspectResponse.get("sub").toString(),
                sessionBody.get("customerId").toString(),
                sessionBody.get("serviceId").toString(),
                introspectResponse.get("client_id").toString(),
                introspectResponse.get("token_type").toString(),
                introspectResponse.get("channel").toString(),
                introspectResponse.get("scope").toString(),
                sessionBody.get("authLevel").toString(),
                "http://localhost:5059/",
                introspectResponse.get("jti").toString()
        );

        return ResponseEntity.ok(Map.of(
                "issued_token", customerJwt,
                "token_type", "SECPJWT"
        ));
    }

    private Map<String, Object> introspectToken(String token) {
        String url = authHost + introspectPath;
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_TYPE, FORM_URLENCODED);
        headers.set(AUTH_HEADER, BASIC + encodedAuth);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("token", token);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, request, Map.class
        );

        log.info("Token Introspect Status: {}", response.getStatusCode());
        return response.getBody();
    }

    private void validateActiveToken(Map<String, Object> response) {
        if (response == null || !Boolean.TRUE.equals(response.get("active"))) {
            throw new AccessDeniedException("Token is not Valid");
        }
    }

    private void validateSessionResponse(Map<String, Object> response) {
        if (response == null || !"200".equals(String.valueOf(response.get("statusCode")))) {
            throw new AccessDeniedException("Session Not Valid");
        }
    }

    private Map<String, Object> getSessionBody(Map<String, Object> response) {
        Object body = response.get("body");
        if (body instanceof Map) {
            return (Map<String, Object>) body;
        }
        throw new AccessDeniedException("Session Not Valid");
    }

    private void validateSessionBody(Map<String, Object> body) {
        if (body == null || body.isEmpty() || !Boolean.parseBoolean(String.valueOf(body.get("isSessionValid")))) {
            throw new AccessDeniedException("Session Not Valid");
        }
    }
}