package com.coolme.tdd;

public class IllegalValueException extends RuntimeException {

    private String option;

    private String value;

    public IllegalValueException(String option, String value) {
        this.option = option;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getOption() {
        return option;
    }
}
