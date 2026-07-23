package com.example.indbank.PolicyDecisionPointService.Controller;

import com.example.indbank.PolicyDecisionPointService.DTO.PolicyEvaluateRequestDTO;
import com.example.indbank.PolicyDecisionPointService.Exceptions.AccessDeniedException;
import com.example.indbank.PolicyDecisionPointService.Exceptions.InvalidChannelException;
import com.example.indbank.PolicyDecisionPointService.Exceptions.MissingQueryParameterException;
import com.example.indbank.PolicyDecisionPointService.Service.PolicyEvaluateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@Tag(
        name = "Policy Evaluation API",
        description = "APIs for evaluating Authorization Policies and generating JWT tokens from SSO Tokens."
)
public class PolicyEvaluateController {

    @Autowired
    private PolicyEvaluateService policyEvaluateService;

    @Operation(
            summary = "Evaluate Authorization Policy",
            description = """
                Evaluates Authorization Policy for a given SSO Token.

                Supported Market:
                • indbank

                Returns a generated JWT if the policy evaluation succeeds.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy evaluated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access Denied"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping(
            value = "/api/v1/realms/{market}/policies/evaluate",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> policyEvaluate(

            @Parameter(
                    description = "Realm / Market",
                    example = "indbank",
                    required = true
            )
            @PathVariable("market")
            String market,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Policy Evaluation Request",
                    content = @Content(
                            schema = @Schema(implementation = PolicyEvaluateRequestDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "application":"AuthorizationPolicyDecisionService",
                                  "resources":["ACCOUNT"],
                                  "channel":"WEB",
                                  "subject":{
                                    "ssoToken":"4be4d5cc-acde-45f2-a0b6-acde12345678"
                                  },
                                  "environment":{
                                    "TokenType":"Bearer"
                                  },
                                  "groupMember":"indbank"
                                }
                                """
                            )
                    )
            )
            @RequestBody
            PolicyEvaluateRequestDTO requestDTO
    ) {
        if (market == null || market.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "market cannot be empty or null!");
        }

        if (!market.equals("indbank")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid market!");
        }

        log.info("Policy Evaluate Request:{}", requestDTO);
        log.info("SSOToken: {}", requestDTO.getSubject().get("ssoToken").toString());
        log.info("Channel: {}", requestDTO.getChannel().get(0).toString());
        policyEvaluateRequestValidate(requestDTO);
        return policyEvaluateService.createSSOTokenToJWT(
                requestDTO.getSubject().get("ssoToken").toString(),
                requestDTO.getChannel().get(0).toString()
        );

    }

    @Operation(
            summary = "Evaluate Root Authorization Policy",
            description = """
                Evaluates Authorization Policy using the legacy Root endpoint.

                Supported Action:
                • evaluate

                Supported Market:
                • indbank
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy evaluated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access Denied"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping(
            value = "/api/v1/json/realms/{market}/root/policies",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> policyEvaluateRoot(

            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "Action",
                    required = true,
                    example = "evaluate",
                    schema = @Schema(
                            allowableValues = {"evaluate"}
                    )
            )
            @RequestParam("_action")
            String action,

            @Parameter(
                    description = "Realm / Market",
                    required = true,
                    example = "indbank"
            )
            @PathVariable("market")
            String market,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Policy Evaluation Request",
                    content = @Content(
                            schema = @Schema(implementation = PolicyEvaluateRequestDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "application":"AuthorizationPolicyDecisionService",
                                  "resources":["ACCOUNT"],
                                  "channel":"WEB",
                                  "subject":{
                                    "ssoToken":"4be4d5cc-acde-45f2-a0b6-acde12345678"
                                  },
                                  "environment":{
                                    "TokenType":"Bearer"
                                  },
                                  "groupMember":"indbank"
                                }
                                """
                            )
                    )
            )
            @RequestBody
            PolicyEvaluateRequestDTO requestDTO
    ) {
        if (market == null || market.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "market cannot be empty or null!");
        }

        if (!market.equals("indbank")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid market!");
        }

        if(action == null || action.isBlank()) {
            throw new MissingQueryParameterException("Missing query parameter: _action");
        }

        if(!action.equals("evaluate")) {
            throw new AccessDeniedException("Invalid query parameter: _action!");
        }

        policyEvaluateRequestValidate(requestDTO);
        return policyEvaluateService.createSSOTokenToJWT(
                requestDTO.getSubject().get("ssoToken").toString(),
                requestDTO.getChannel().get(0)
        );

    }

    public void policyEvaluateRequestValidate(PolicyEvaluateRequestDTO requestDTO) {
        if(requestDTO.getApplication() == null ||  requestDTO.getApplication().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application cannot be empty!");
        }

        if(!requestDTO.getApplication().equals("AuthorizationPolicyDecisionService")){
            throw new AccessDeniedException("Invalid Application name provided!");
        }

        if(requestDTO.getResources().isEmpty() || !requestDTO.getResources().contains("APIResource")){
            throw new AccessDeniedException("No resources provided!");
        }

        if(requestDTO.getChannel() == null || requestDTO.getChannel().isEmpty()){
            throw new AccessDeniedException("ChannelType cannot be empty or null!");
        }

        if (!requestDTO.getChannel().contains("WEB") && !requestDTO.getChannel().contains("MOBILE")) {
            throw new InvalidChannelException("ChannelType must be WEB or MOBILE!");
        }


        if(requestDTO.getSubject() == null || requestDTO.getSubject().isEmpty()){
            throw  new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject cannot be empty or null!");
        }

        if(requestDTO.getSubject().get("ssoToken") == null || requestDTO.getSubject().get("ssoToken").toString().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ssoToken cannot be empty or null!");
        }

        if(requestDTO.getEnvironment().get("TokenType") == null || requestDTO.getEnvironment().get("TokenType").toString().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TokenType cannot be empty or null!");
        }

        if(requestDTO.getGroupMember() == null || requestDTO.getGroupMember().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "GroupMember cannot be empty or null!");
        }

        if(!requestDTO.getGroupMember().contains("indbank")){
            throw new AccessDeniedException("Invalid group member!");
        }
    }
}
