package com.coolme.args;

public class ToManyArgumentsException extends RuntimeException {


  private String option;

  public ToManyArgumentsException(String option) {
    this.option = option;
  }

  public String getOption() {
    return option;
  }

  public void setOption(String option) {
    this.option = option;
  }
}
