package com.example.indbank.PolicyDecisionPointService.Exceptions;

public class AccessDeniedException extends RuntimeException {
  public AccessDeniedException(String message) {
    super(message);
  }
}
