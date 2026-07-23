package com.example.indbank.PolicyDecisionPointService.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogOffProviderRestSTSDTO {

    @JsonProperty(value = "input_token_state", required = true)
    private Input_Token_State inputTokenState;

    @JsonProperty(value = "output_token_state", required = true)
    private Output_Token_State outputTokenState;

}
