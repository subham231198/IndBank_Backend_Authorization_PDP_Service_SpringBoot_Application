package com.example.indbank.PolicyDecisionPointService.Controller;

import com.example.indbank.PolicyDecisionPointService.DTO.OAuth2_RequestDTO;
import com.example.indbank.PolicyDecisionPointService.Exceptions.AccessDeniedException;
import com.example.indbank.PolicyDecisionPointService.Service.PolicyEvaluateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AccessTokenCustomerJwtController {

    @Autowired
    private PolicyEvaluateService policyEvaluateService;

    private static final String ACCESSTOKEN = "ACCESSTOKEN";
    private static final String SECPJWT = "SECPJWT";
    private static final String BEARER = "Bearer";

    @PostMapping(
            value = "/v1/api/oauth/accessToken_to_customerJwt",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    private ResponseEntity<?> accessTokenCustomerJwt(@RequestBody OAuth2_RequestDTO tokenConversion) {
        validateRequest(tokenConversion);
        return policyEvaluateService.createAccessTokenToCustomerJwt(
                tokenConversion.getInputTokenState().getTokenId()
        );
    }

    private void validateRequest(OAuth2_RequestDTO request) {
        if (request.getInputTokenState() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "input_token_state cannot be null or empty!");
        }

        if (request.getOutputTokenState() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "output_token_state cannot be null or empty!");
        }

        String tokenType = request.getInputTokenState().getToken_type();
        if (tokenType == null || tokenType.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "input_token_type cannot be null or empty!");
        }

        if (!ACCESSTOKEN.equals(tokenType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid value input_token_type");
        }

        String tokenId = request.getInputTokenState().getTokenId();
        if (tokenId == null || tokenId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tokenId cannot be null or empty!");
        }

        String outputTokenType = request.getOutputTokenState().getOutput_token_type();
        if (!SECPJWT.equals(outputTokenType)) {
            throw new AccessDeniedException("Invalid output_token_type!");
        }

        String message = request.getOutputTokenState().getMessage();
        if (message == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "output_token_state_message cannot be null or empty!");
        }

        if (!BEARER.equals(message)) {
            throw new AccessDeniedException("Invalid message provided!");
        }
    }
}