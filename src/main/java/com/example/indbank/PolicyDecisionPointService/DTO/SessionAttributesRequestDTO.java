package com.example.indbank.PolicyDecisionPointService.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionAttributesRequestDTO {

    @JsonProperty(value = "tokenId")
    private String tokenId;
}
