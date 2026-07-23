package com.example.indbank.PolicyDecisionPointService.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PolicyEvaluateResponseDTO {

    @JsonProperty(value = "application")
    private String application;

    @JsonProperty(value = "resource")
    private String resource;

    @JsonProperty(value = "actions")
    private Map<String, Object> actions;

    @JsonProperty(value = "correlationId")
    private List<String> correlationId;

    @JsonProperty(value = "issued_token")
    private List<String> issuedToken;
}
