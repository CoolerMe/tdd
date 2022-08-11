package com.coolme.tdd.exception;

/**
 * @author coolme
 */
public class IllegalOptionException extends RuntimeException {


  private String parameter;

  public IllegalOptionException(String parameter) {
    this.parameter = parameter;
  }

  public String getParameter() {
    return parameter;
  }


}
