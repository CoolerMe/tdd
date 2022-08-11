package com.coolme.tdd;

/**
 * @author coolme
 */
public class IllegalOptionException extends RuntimeException {


  public String getParameter() {
    return parameter;
  }

  private String parameter;

  public IllegalOptionException(String parameter) {
    this.parameter = parameter;
  }


}
