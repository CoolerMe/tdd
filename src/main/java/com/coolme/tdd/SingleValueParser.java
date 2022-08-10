package com.coolme.tdd;

import java.util.List;
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
    int index = arguments.indexOf("-" + option.value());

    if (index == -1) {
      return defaultValue;
    }

    int size = getOptionSize(arguments, index);

    if (size < 1) {
      throw new InsufficientArgumentsException(option.value());
    }

    if (size > 1) {
      throw new TooManyArgumentsException(option.value());
    }

    String value = arguments.get(index + 1);
    return parser.apply(value);
  }

  private static int getOptionSize(List<String> arguments, int index) {
    int followedIndex = IntStream.range(index + 1, arguments.size())
        .filter(it -> arguments.get(it).startsWith("-"))
        .findFirst().orElse(arguments.size());

    List<String> values = arguments.subList(index + 1, followedIndex);

    return values.size();
  }

}
