package com.example.indbank.PolicyDecisionPointService.Exceptions;

import org.springframework.boot.webmvc.autoconfigure.error.AbstractErrorController;
import org.springframework.boot.webmvc.autoconfigure.error.BasicErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        int code = 0;
        String reason = "";
        switch (ex.getStatusCode().value()){
            case 400: code = 400;
                reason = "Bad Request";
                break;

            case 401: code = 401;
                reason = "Unauthorized";
                break;

            case 403: code = 403;
                reason = "Forbidden";
                break;

            case 404: code = 404;
                reason = "Not Found";
                break;

            case 405: code = 405;
                reason = "Method Not Allowed";
                break;

            case 500: code = 500;
                reason = "Internal Server Error";
                break;

            case 501: code = 501;
                reason = "Not Acceptable";
                break;

            case 502: code = 502;
                reason = "Bad Gateway";
                break;

            case 503: code = 503;
                reason = "Service Unavailable";
                break;
        }
        responseBody.put("code", code);
        responseBody.put("reason", reason);
        responseBody.put("message", ex.getReason());
        return new  ResponseEntity<>(responseBody, ex.getStatusCode());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("code", 401);
        responseBody.put("reason", "Unauthorized");
        responseBody.put("message", "Access denied");
        ex.printStackTrace();
        return ResponseEntity.status(401).body(responseBody);
    }

    @ExceptionHandler(MissingQueryParameterException.class)
    public ResponseEntity<?> handleMissingQueryParamException(Exception ex) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("code", 400);
        responseBody.put("reason", "Bad Request");
        responseBody.put("message", "Missing required query parameter");
        ex.printStackTrace();
        return ResponseEntity.status(400).body(responseBody);
    }

    @ExceptionHandler(MissingSessionIdException.class)
    public ResponseEntity<?> handleMissingSessionIdException(Exception ex){
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("code", 400);
        responseBody.put("reason", "Bad Request");
        responseBody.put("message", "tokenId cannot be empty or null!");
        ex.printStackTrace();
        return ResponseEntity.status(400).body(responseBody);
    }

    @ExceptionHandler(InvalidSessionException.class)
    public ResponseEntity<?> handleInvalidSessionException(InvalidSessionException ex) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("isSessionValid", false);
        return ResponseEntity.status(200).body(responseBody);
    }

    @ExceptionHandler(InvalidSessionPatternException.class)
    public ResponseEntity<?> handleInvalidSessionPatternException(InvalidSessionPatternException ex) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("code", 400);
        responseBody.put("reason", "Bad Request");
        responseBody.put("message", "Invalid tokenId provided!");
        return ResponseEntity.status(400).body(responseBody);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<?> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("code", 400);
        responseBody.put("reason", "Bad Request");
        responseBody.put("message", ex.getMessage());
        return ResponseEntity.status(400).body(responseBody);
    }

    @ExceptionHandler(LogOffProviderException.class)
    public ResponseEntity<?> handleLogOffProviderException(LogOffProviderException ex) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("code", 401);
        responseBody.put("reason", "Unauthorized");
        responseBody.put("message", "Error resolving user from JSON");
        return ResponseEntity.status(400).body(responseBody);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("code", 400);
        responseBody.put("reason", "Bad Request");
        responseBody.put("message", ex.getMessage());
        return ResponseEntity.status(400).body(responseBody);
    }

    @ExceptionHandler(InvalidChannelException.class)
    public ResponseEntity<?> handleInvalidChannelException(InvalidChannelException ex) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("code", 401);
        responseBody.put("reason", "Unauthorized");
        responseBody.put("message", ex.getMessage());
        return ResponseEntity.status(401).body(responseBody);
    }

}
