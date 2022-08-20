package com.coolme;

import java.util.List;
import java.util.function.Function;

public class SingleValueOptionParser<T> implements OptionParser<T> {

  Function<String, T> valueParser;

  private T defaultValue;

  public SingleValueOptionParser(T defaultValue, Function<String, T> valueParser) {
    this.valueParser = valueParser;
    this.defaultValue = defaultValue;
  }


  @Override
  public T parse(List<String> arguments, Option option) {
    int index = arguments.indexOf("-" + option.value());

    if (index == 0) {
      return defaultValue;
    }

    if (index + 1 == arguments.size() || arguments.get(index + 1).startsWith("-")) {
      throw new InsufficientArgumentsException(option.value());
    }

    if (index + 2 < arguments.size() && !arguments.get(index + 2).startsWith("-")) {
      throw new ToManyArgumentsException(option.value());
    }

    String value = arguments.get(index + 1);
    return parseValue(value);
  }

  private T parseValue(String value) {
    return valueParser.apply(value);
  }

}
