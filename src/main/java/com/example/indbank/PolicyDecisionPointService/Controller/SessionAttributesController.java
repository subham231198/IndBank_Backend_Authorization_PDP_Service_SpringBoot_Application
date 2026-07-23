package com.example.indbank.PolicyDecisionPointService.Controller;

import com.example.indbank.PolicyDecisionPointService.DTO.SessionAttributesRequestDTO;
import com.example.indbank.PolicyDecisionPointService.Exceptions.AccessDeniedException;
import com.example.indbank.PolicyDecisionPointService.Exceptions.InvalidChannelException;
import com.example.indbank.PolicyDecisionPointService.Exceptions.MissingQueryParameterException;
import com.example.indbank.PolicyDecisionPointService.Service.SessionAttributesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;


@RestController
@RequestMapping("/api/v1/info/sessions")
@Tag(
        name = "Session Attributes API",
        description = "APIs for retrieving customer session information."
)
public class SessionAttributesController {

    @Autowired
    private SessionAttributesService sessionAttributesService;

    @Value("${sessionInfo.clientId}")
    private String clientId1;

    @Value("${sessionInfo.clientSecret}")
    private String clientSecret1;

    @Value("${sessionInfo.all.clientId}")
    private String clientId2;

    @Value("${sessionInfo.all.clientSecret}")
    private String clientSecret2;

    @Operation(
            summary = "Retrieve Session Information",
            description = """
                    Retrieves customer session information.

                    Supported Actions:
                    • getSessionInfo
                    • getAllSessionInfo

                    Authorization header must contain Base64 encoded
                    clientId:clientSecret.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session information retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access Denied"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> getSessionInfo(

            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Action to perform",
                    required = true,
                    example = "getSessionInfo",
                    schema = @Schema(
                            allowableValues = {
                                    "getSessionInfo",
                                    "getAllSessionInfo"
                            }
                    )
            )
            @RequestParam("_action")
            String action,

            @Parameter(
                    in = ParameterIn.HEADER,
                    description = "Base64 encoded clientId:clientSecret",
                    required = true,
                    example = "Y2xpZW50SWQ6Y2xpZW50U2VjcmV0"
            )
            @RequestHeader("Authorization")
            String authorization,

            @Parameter(
                    in = ParameterIn.HEADER,
                    description = "Customer Session Id",
                    required = true,
                    example = "f5ad76b8-2e4a-44d8-9e90-123456789abc"
            )
            @RequestHeader("X-CustomerSessionId")
            String customerSessionId,

            @Parameter(
                    in = ParameterIn.HEADER,
                    description = "Application Channel",
                    required = true,
                    example = "WEB",
                    schema = @Schema(
                            allowableValues = {
                                    "WEB",
                                    "MOBILE"
                            }
                    )
            )
            @RequestHeader("X-Channel")
            String channel,

            @RequestBody(
                    description = "Session Attributes Request",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = SessionAttributesRequestDTO.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "tokenId":"f5ad76b8-2e4a-44d8-9e90-123456789abc"
                                            }
                                            """
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody
            SessionAttributesRequestDTO sessionAttributesRequestDTO
    ) {

        if (action == null || action.isBlank()) {
            throw new MissingQueryParameterException(
                    "Missing query parameter: _action"
            );
        }

        if (!action.equals("getSessionInfo")
                && !action.equals("getAllSessionInfo")) {
            throw new AccessDeniedException(
                    "Invalid query parameter: _action!"
            );
        }

        if(authorization == null || authorization.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization Required");
        }
        if (!validateSessionAttributesSecret(action, authorization)) {
            throw new AccessDeniedException(
                    "Invalid Session Attributes Authorization Secrets!"
            );
        }

        if (channel == null || channel.isBlank()) {
            throw new AccessDeniedException(
                    "X-Channel cannot be empty or null!"
            );
        }

        if (!channel.equals("WEB")
                && !channel.equals("MOBILE")) {
            throw new InvalidChannelException(
                    "Channel must be WEB or MOBILE!"
            );
        }

        if (sessionAttributesRequestDTO.getTokenId() == null
                || sessionAttributesRequestDTO.getTokenId().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "tokenId cannot be empty or null!"
            );
        }

        if (!customerSessionId.equals(
                sessionAttributesRequestDTO.getTokenId())) {

            throw new AccessDeniedException(
                    "tokenId in header and body must be same!"
            );
        }

        if (action.equals("getSessionInfo")) {
            return sessionAttributesService.getSessionInfo(
                    sessionAttributesRequestDTO.getTokenId(),
                    channel
            );
        }

        return sessionAttributesService.getAllSessionInfo(
                sessionAttributesRequestDTO.getTokenId(),
                channel
        );
    }

    private Boolean validateSessionAttributesSecret(
            String action,
            String authorization) {

        if (authorization == null || authorization.isBlank()) {
            throw new AccessDeniedException(
                    "Authorization header is missing!"
            );
        }

        if(!authorization.startsWith("Basic ")){
            throw new AccessDeniedException("Invalid Authorization Header!");
        }

        String encodedCredentials = authorization;

        if (authorization.startsWith("Basic ")) {
            encodedCredentials = authorization.substring(6);
        }

        String decodedCredentials;

        try {
            decodedCredentials = new String(
                    Base64.getDecoder().decode(encodedCredentials),
                    StandardCharsets.UTF_8
            );
        } catch (IllegalArgumentException ex) {
            throw new AccessDeniedException(
                    "Invalid Authorization Header!"
            );
        }

        if (!decodedCredentials.contains(":")) {
            throw new AccessDeniedException(
                    "Invalid Authorization Header!"
            );
        }

        String[] credentials = decodedCredentials.split(":", 2);

        String actualClientId = credentials[0];
        String actualClientSecret = credentials[1];

        if ("getSessionInfo".equals(action)) {
            return actualClientId.equals(clientId1)
                    && actualClientSecret.equals(clientSecret1);
        }

        if ("getAllSessionInfo".equals(action)) {
            return actualClientId.equals(clientId2)
                    && actualClientSecret.equals(clientSecret2);
        }

        return false;
    }
}