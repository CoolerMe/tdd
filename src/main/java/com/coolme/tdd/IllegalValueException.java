package com.coolme.tdd;

/**
 * @author coolme
 */
public class IllegalValueException extends RuntimeException {

  private String optionValue;
  private String value;

  public String getOptionValue() {
    return optionValue;
  }

  public void setOptionValue(String optionValue) {
    this.optionValue = optionValue;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public IllegalValueException(String optionValue, String value) {
    this.optionValue = optionValue;
    this.value = value;
  }


}
