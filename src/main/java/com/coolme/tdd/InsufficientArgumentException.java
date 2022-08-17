package com.coolme.tdd;

public class InsufficientArgumentException extends RuntimeException {

    private String option;

    public InsufficientArgumentException(String option) {
        this.option = option;
    }

    public String getOption() {
        return option;
    }
}
