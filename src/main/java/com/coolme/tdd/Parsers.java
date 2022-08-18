package com.coolme.tdd;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

/**
 * @author coolme
 */
public class Parsers {

    public static OptionParser<Boolean> bool() {
        return (arguments, option) -> values(arguments, option, 0).isPresent();
    }

    public static <T> OptionParser<T> unary(T defaultValue, Function<String, T> parser) {
        return (arguments, option) -> values(arguments, option, 1).map(
            values -> parseValue(option, values.get(0), parser)).orElse(defaultValue);
    }

    public static <T> OptionParser<T[]> list(IntFunction<T[]> generator,
        Function<String, T> parser) {
        return (arguments, option) -> values(arguments, option).map(
                strings -> strings.stream().map(s -> parseValue(option, s, parser)).toArray(generator))
            .orElse(generator.apply(0));
    }

    private static Optional<List<String>> values(List<String> arguments, Option option,
        int expectedSize) {
        return values(arguments, option)
            .map(values -> checkSize(option, expectedSize, values));
    }

    private static List<String> checkSize(Option option, int expectedSize, List<String> values) {
        if (values.size() < expectedSize) {
            throw new InsufficientArgumentException(option.value());
        }

        if (values.size() > expectedSize) {
            throw new TooManyArgumentException(option.value());
        }
        return values;
    }

    private static Optional<List<String>> values(List<String> arguments, Option option) {
        int index = arguments.indexOf("-" + option.value());
        return Optional.ofNullable(index == -1 ? null : values(arguments, index));
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
            .filter(it -> arguments.get(it).matches("^-[a-zA-Z-]+$"))
            .findFirst()
            .orElse(arguments.size());

        return arguments.subList(index + 1, followedIndex);
    }

}
