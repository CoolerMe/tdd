package com.coolme.tdd.exception;

/**
 * @author coolme
 */
public class InsufficientArgumentsException extends RuntimeException {

  private String value;

  public InsufficientArgumentsException(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return getMessage() + ",value:" + value;
  }
}
