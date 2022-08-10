package com.coolme.tdd;

/**
 * @author coolme
 */
public class InsufficientArgumentsException extends RuntimeException {

  private String value;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public InsufficientArgumentsException(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return getMessage() + ",value:" + value;
  }
}
