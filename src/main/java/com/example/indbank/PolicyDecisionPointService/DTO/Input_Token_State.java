package com.example.indbank.PolicyDecisionPointService.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Input_Token_State {

    @JsonProperty(value = "token_type", required = true)
    private String token_type;

    @JsonProperty(value = "tokenId", required = true)
    private String tokenId;
}
