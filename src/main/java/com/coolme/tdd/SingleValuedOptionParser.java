package com.coolme.tdd;

import java.util.List;
import java.util.function.Function;

public class SingleValuedOptionParser<T> implements OptionParser<T> {

    private final Function<String, T> parser;

    private T defaultValue;

    public SingleValuedOptionParser(T defaultValue, Function<String, T> parser) {
        this.defaultValue = defaultValue;
        this.parser = parser;
    }

    @Override
    public T parse(List<String> arguments, Option option) {
        int index = arguments.indexOf("-" + option.value());

        if (index == -1) {
            return defaultValue;
        }

        if (index + 1 == arguments.size() || arguments.get(index + 1).startsWith("-")) {
            throw new InsufficientArgumentException(option.value());
        }

        if (index + 2 < arguments.size() && !arguments.get(index + 2).startsWith("-")) {
            throw new TooManyArgumentException(option.value());
        }

        String value = arguments.get(index + 1);
        return parser.apply(value);
    }

}
