package com.example.indbank.PolicyDecisionPointService.Exceptions;

public class MissingQueryParameterException extends RuntimeException {
  public MissingQueryParameterException(String message) {
    super(message);
  }
}
