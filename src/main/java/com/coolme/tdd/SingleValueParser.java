package com.coolme.tdd;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

class SingleValueParser<T> implements OptionParser<T> {

  private final Function<String, T> parser;

  private T defaultValue;

  public SingleValueParser(T defaultValue, Function<String, T> parser) {
    this.defaultValue = defaultValue;
    this.parser = parser;
  }

  @Override
  public T parse(List<String> arguments, Option option) {

    return values(arguments, option, 1)
        .map(strings -> parseValue(option, strings.get(0)))
        .orElse(defaultValue);

  }

  public static Optional<List<String>> values(List<String> arguments, Option option,
      int expectedSize) {

    Optional<List<String>> optional;
    int index = arguments.indexOf("-" + option.value());

    if (index == -1) {
      optional = Optional.empty();
    } else {
      List<String> values = values(arguments, index);

      if (values.size() < expectedSize) {
        throw new InsufficientArgumentsException(option.value());
      }

      if (values.size() > expectedSize) {
        throw new TooManyArgumentsException(option.value());
      }
      optional = Optional.of(values);
    }
    return optional;
  }

  private T parseValue(Option option, String value) {

    try {
      return parser.apply(value);
    } catch (Exception e) {
      throw new IllegalValueException(option.value(), value);
    }
  }

  public static List<String> values(List<String> arguments, int index) {
    int followedIndex = IntStream
        .range(index + 1, arguments.size())
        .filter(it -> arguments.get(it).startsWith("-"))
        .findFirst().orElse(arguments.size());

    return arguments.subList(index + 1, followedIndex);
  }

}
