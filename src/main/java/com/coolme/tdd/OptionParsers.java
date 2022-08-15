package com.coolme.tdd;

import com.coolme.tdd.exception.IllegalOptionException;
import com.coolme.tdd.exception.IllegalValueException;
import com.coolme.tdd.exception.InsufficientArgumentsException;
import com.coolme.tdd.exception.TooManyArgumentsException;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

class OptionParsers<T> {

    public static OptionParser<Boolean> bool() {
        return (arguments, option) -> values(arguments, option, 0).isPresent();
    }

    public static <T> OptionParser<T> unary(T defaultValue, Function<String, T> parser) {
        return (arguments, option) -> values(arguments, option, 1).map(
            strings -> parseValue(option, strings.get(0), parser)).orElse(defaultValue);
    }

    public static <T> OptionParser<T[]> list(IntFunction<T[]> generator,
        Function<String, T> parser) {
        return (arguments, option)
            -> values(arguments, option)
            .map(it ->
                it.stream()
                    .map(value -> parseValue(option, value, parser))
                    .toArray(generator))
            .orElse(generator.apply(0));

    }




    private static Optional<List<String>> values(List<String> arguments, Option option) {
        int index = arguments.indexOf("-" + option.value());
        return Optional.ofNullable(index == -1 ? null : values(arguments, index));
    }

    private static Optional<List<String>> values(List<String> arguments, Option option,
        int expectedSize) {
        return values(arguments, option)
            .map(strings -> checkSize(option, expectedSize, strings));
    }

    private static List<String> checkSize(Option option, int expectedSize, List<String> values) {
        if (values.size() < expectedSize) {
            throw new InsufficientArgumentsException(option.value());
        }

        if (values.size() > expectedSize) {
            throw new TooManyArgumentsException(option.value());
        }

        return values;

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
            .filter(it -> arguments.get(it).matches("^-[a-zA-z-]+$"))
            .findFirst().orElse(arguments.size());

        return arguments.subList(index + 1, followedIndex);
    }

}
