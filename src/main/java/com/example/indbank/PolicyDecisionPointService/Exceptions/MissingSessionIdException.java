package com.example.indbank.PolicyDecisionPointService.Exceptions;

public class MissingSessionIdException extends RuntimeException {
    public MissingSessionIdException(String message) {
        super(message);
    }
}
