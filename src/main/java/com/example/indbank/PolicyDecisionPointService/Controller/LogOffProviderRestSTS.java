package com.example.indbank.PolicyDecisionPointService.Controller;

import com.example.indbank.PolicyDecisionPointService.DTO.LogOffProviderRestSTSDTO;
import com.example.indbank.PolicyDecisionPointService.Exceptions.AccessDeniedException;
import com.example.indbank.PolicyDecisionPointService.Exceptions.MissingQueryParameterException;
import com.example.indbank.PolicyDecisionPointService.Service.LogOffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/rest-sts/logoff")
@Tag(
        name = "REST STS LogOff API",
        description = "API for invalidating/logout of REST STS Bearer Tokens."
)
public class LogOffProviderRestSTS {

    @Autowired
    private LogOffService logOffService;

    @Operation(
            summary = "Log Off REST STS Token",
            description = """
                    Invalidates an existing REST STS Bearer Token.

                    Supported Action:
                    • translate

                    The input token will be invalidated and removed from the active session.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token logged off successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access Denied"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> logOffProvider(

            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Action",
                    required = true,
                    example = "translate",
                    schema = @Schema(
                            allowableValues = {"translate"}
                    )
            )
            @RequestParam("_action")
            String action,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "REST STS LogOff Request",
                    content = @Content(
                            schema = @Schema(
                                    implementation = LogOffProviderRestSTSDTO.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "inputTokenState": {
                                                "token_type": "Bearer",
                                                "tokenId": "f5ad76b8-2e4a-44d8-9e90-123456789abc"
                                              },
                                              "outputTokenState": {
                                                "output_token_type": "",
                                                "message": "Bearer"
                                              }
                                            }
                                            """
                            )
                    )
            )
            @RequestBody
            LogOffProviderRestSTSDTO logOffProviderRestSTSDTO
    ) {

        if (action == null || action.isBlank()) {
            throw new MissingQueryParameterException(
                    "Missing query parameter: _action"
            );
        }

        if (!action.equals("translate")) {
            throw new AccessDeniedException(
                    "Invalid query parameter: _action!"
            );
        }

        if (logOffProviderRestSTSDTO.getInputTokenState() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "input_token_state cannot be null or empty!"
            );
        }

        if (logOffProviderRestSTSDTO.getOutputTokenState() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "output_token_state cannot be null or empty!"
            );
        }

        if (logOffProviderRestSTSDTO.getInputTokenState().getToken_type() == null || logOffProviderRestSTSDTO.getInputTokenState().getToken_type().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "input_token_type cannot be null or empty!"
            );
        }

        if(!logOffProviderRestSTSDTO.getInputTokenState().getToken_type().equals("SSOTOKEN")){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "invalid value input_token_type");
        }
        if (logOffProviderRestSTSDTO.getInputTokenState().getTokenId() == null || logOffProviderRestSTSDTO.getInputTokenState().getTokenId().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "tokenId cannot be null or empty!"
            );
        }

        if (!logOffProviderRestSTSDTO.getOutputTokenState().getOutput_token_type().equals("")) {
            throw new AccessDeniedException(
                    "Invalid output_token_type!"
            );
        }

        if (logOffProviderRestSTSDTO.getOutputTokenState().getMessage() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "output_token_state_message cannot be null or empty!"
            );
        }

        if (!logOffProviderRestSTSDTO.getOutputTokenState().getMessage().equals("Bearer")) {
            throw new AccessDeniedException(
                    "Invalid message provided!"
            );
        }

        return logOffService.logOff(
                logOffProviderRestSTSDTO.getInputTokenState().getTokenId()
        );
    }
}