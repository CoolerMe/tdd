package com.coolme.tdd;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

class OptionParsers<T> {

  public static <T> OptionParser<T> unary(T defaultValue,
      Function<String, T> parser) {
    return (arguments, option) -> values(arguments, option, 1)
        .map(strings -> parseValue(option, strings.get(0), parser))
        .orElse(defaultValue);
  }

  public static OptionParser<Boolean> bool() {
    return (arguments, option)
        -> values(arguments, option, 0)
        .map(it -> true)
        .orElse(false);
  }

  static Object parse(List<String> arguments, Parameter parameter) {
    if (!parameter.isAnnotationPresent(Option.class)) {
      throw new IllegalOptionException(parameter.getName());
    }
    return Args.PARSER.get(parameter.getType())
        .parse(arguments, parameter.getAnnotation(Option.class));
  }


  private static Optional<List<String>> values(List<String> arguments, Option option,
      int expectedSize) {

    int index = arguments.indexOf("-" + option.value());

    if (index == -1) {
      return Optional.empty();
    }

    List<String> values = values(arguments, index);
    if (values.size() < expectedSize) {
      throw new InsufficientArgumentsException(option.value());
    }

    if (values.size() > expectedSize) {
      throw new TooManyArgumentsException(option.value());
    }

    return Optional.of(values);
  }


  private static <T> T parseValue(Option option, String value, Function<String, T> parser) {

    try {
      return parser.apply(value);
    } catch (Exception e) {
      throw new IllegalValueException(option.value(), value);
    }
  }

  private static List<String> values(List<String> arguments, int index) {
    int followedIndex = IntStream
        .range(index + 1, arguments.size())
        .filter(it -> arguments.get(it).startsWith("-"))
        .findFirst()
        .orElse(arguments.size());

    return arguments.subList(index + 1, followedIndex);
  }

}
