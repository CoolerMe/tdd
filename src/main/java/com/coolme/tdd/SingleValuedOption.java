package com.coolme.tdd;

import java.util.List;
import java.util.function.Function;

public class SingleValuedOption implements OptionParser {

    Function<String, Object> parser;

    public SingleValuedOption(Function<String, Object> parser) {
        this.parser = parser;
    }

    @Override
    public Object parse(List<String> arguments, Option option) {
        int index = arguments.indexOf("-" + option.value());
        String value = arguments.get(index + 1);
        return parser.apply(value);
    }

}
