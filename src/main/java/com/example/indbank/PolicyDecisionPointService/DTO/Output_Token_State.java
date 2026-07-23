package com.example.indbank.PolicyDecisionPointService.DTO;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Output_Token_State {

    @JsonProperty(value = "token_type", required = true)
    private String output_token_type;

    @JsonProperty(value = "message", required = true)
    private String message;
}
