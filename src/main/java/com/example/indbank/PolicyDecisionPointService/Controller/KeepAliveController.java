package com.example.indbank.PolicyDecisionPointService.Controller;

import com.example.indbank.PolicyDecisionPointService.DTO.SessionAttributesRequestDTO;
import com.example.indbank.PolicyDecisionPointService.Exceptions.AccessDeniedException;
import com.example.indbank.PolicyDecisionPointService.Exceptions.InvalidChannelException;
import com.example.indbank.PolicyDecisionPointService.Exceptions.MissingQueryParameterException;
import com.example.indbank.PolicyDecisionPointService.Service.KeepAliveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/sessions")
@Tag(
        name = "Keep Alive API",
        description = "APIs for validating and extending customer sessions."
)
public class KeepAliveController {

    @Autowired
    private KeepAliveService keepAliveService;

    @Operation(
            summary = "Keep customer session alive",
            description = """
                    Validates an existing customer session and returns whether the
                    session is still active along with the correlationId when valid.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Session validated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access Denied"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error"
            )
    })
    @PostMapping(
            value = "/keepAlive",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> keepAliveInternetFacing(

            @Parameter(
                    description = "Customer Session Id",
                    example = "f5ad76b8-2e4a-44d8-9e90-123456789abc",
                    required = true
            )
            @RequestHeader("X-CustomerSessionId")
            String customerSessionId,

            @Parameter(
                    description = "Application Channel",
                    example = "WEB",
                    schema = @Schema(allowableValues = {"WEB", "MOBILE"})
            )
            @RequestHeader("X-Channel")
            String channel,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Keep Alive Request",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "tokenId":"f5ad76b8-2e4a-44d8-9e90-123456789abc"
                                            }
                                            """
                            )
                    )
            )
            @Valid
            @RequestBody
            SessionAttributesRequestDTO sessionAttributesRequestDTO
    ) {

        if (channel == null || channel.isBlank()) {
            throw new AccessDeniedException("X-Channel cannot be empty or null!");
        }

        if (!channel.equals("WEB") && !channel.equals("MOBILE")) {
            throw new InvalidChannelException("Channel must be WEB or MOBILE!");
        }

        if (sessionAttributesRequestDTO.getTokenId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "tokenId cannot be empty or null!"
            );
        }

        if (!customerSessionId.equals(sessionAttributesRequestDTO.getTokenId())) {
            throw new AccessDeniedException(
                    "tokenId in header and body must be same!"
            );
        }

        return keepAliveService.keepAlive(
                sessionAttributesRequestDTO.getTokenId(),
                channel
        );
    }

    @Operation(
            summary = "Keep customer session alive (Action API)",
            description = "Validates an existing customer session using action=keepAlive."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session validated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Access Denied"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> keepAliveAction(

            @RequestParam(value = "_action", required = true) String action,
            @RequestHeader("X-CustomerSessionId")
            String customerSessionId,

            @RequestHeader("X-Channel")
            String channel,

            @Valid
            @RequestBody
            SessionAttributesRequestDTO sessionAttributesRequestDTO
    ) {

        if(action == null || action.isBlank()) {
            throw new MissingQueryParameterException("Missing query parameter: _action");
        }

        if(!action.equals("keepAlive")) {
            throw new AccessDeniedException("Invalid query parameter: _action!");
        }
        if (channel == null || channel.isBlank()) {
            throw new AccessDeniedException("X-Channel cannot be empty or null!");
        }

        if (!channel.equals("WEB") && !channel.equals("MOBILE")) {
            throw new InvalidChannelException("Channel must be WEB or MOBILE!");
        }

        if (sessionAttributesRequestDTO.getTokenId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "tokenId cannot be empty or null!"
            );
        }

        if (!customerSessionId.equals(sessionAttributesRequestDTO.getTokenId())) {
            throw new AccessDeniedException(
                    "tokenId in header and body must be same!"
            );
        }

        return keepAliveService.keepAlive(
                sessionAttributesRequestDTO.getTokenId(),
                channel
        );
    }
}