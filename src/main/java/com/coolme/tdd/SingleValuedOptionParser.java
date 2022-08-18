package com.coolme.tdd;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

public class SingleValuedOptionParser {

    public static OptionParser<Boolean> bool() {
        return (arguments, option)
            -> values(arguments, option, 0)
            .isPresent();
    }

    public static <T> OptionParser<T> unary(T defaultValue, Function<String, T> parser) {
        return (arguments, option) -> values(arguments, option, 1)
            .map(values -> parseValue(option, values.get(0), parser))
            .orElse(defaultValue);
    }

    private static Optional<List<String>> values(List<String> arguments, Option option,
        int expectedSize) {
        Optional<List<String>> optional;
        int index = arguments.indexOf("-" + option.value());

        if (index == -1) {
            optional = Optional.empty();
        } else {
            List<String> values = values(arguments, index);

            if (values.size() < expectedSize) {
                throw new InsufficientArgumentException(option.value());
            }

            if (values.size() > expectedSize) {
                throw new TooManyArgumentException(option.value());
            }

            optional = Optional.of(values);
        }
        return optional;
    }

    private static <T> T parseValue(Option option, String value, Function<String, T> parser) {
        try {
            return parser.apply(value);
        } catch (Exception e) {
            throw new IllegalValueException(option.value(), value);
        }
    }

    private static List<String> values(List<String> arguments, int index) {
        int followedIndex = IntStream.range(index + 1, arguments.size())
            .filter(it -> arguments.get(it).startsWith("-"))
            .findFirst()
            .orElse(arguments.size());

        return arguments.subList(index + 1, followedIndex);
    }

}
