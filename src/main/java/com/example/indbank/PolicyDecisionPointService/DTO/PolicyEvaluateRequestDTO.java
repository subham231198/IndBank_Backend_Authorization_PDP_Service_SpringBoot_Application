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
public class PolicyEvaluateRequestDTO {

    @JsonProperty(value = "resources")
    private List<String> resources;

    @JsonProperty(value = "application")
    private String application;

    @JsonProperty(value = "subject")
    private Map<String, Object> subject;

    @JsonProperty(value = "environment")
    private Map<String, Object> environment;

    @JsonProperty(value = "ChannelType")
    private List<String> channel;

    @JsonProperty(value = "GroupMemeberCode")
    private List<String> groupMember;
}
