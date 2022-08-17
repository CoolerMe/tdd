package com.coolme.tdd;

import java.util.List;

interface OptionParser<T> {

    /**
     * Parse arguments to get option
     *
     * @param arguments list of argument
     * @param option    {@link Option}
     * @return the result
     */
    T parse(List<String> arguments, Option option);
}
