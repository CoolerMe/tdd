package com.coolme.tdd;

/**
 * @author coolme
 */
public class TooManyArgumentsException extends RuntimeException {

  private String value;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public TooManyArgumentsException(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return getMessage() + ",value:" + value;
  }
}
