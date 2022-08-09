package com.coolme.tdd;

import com.coolme.tdd.Args.OptionParser;
import java.util.List;
import java.util.function.Function;

class SingleValueParser<T> implements OptionParser {

  private final Function<String, T> parser;

  public SingleValueParser(Function<String, T> parser) {
    this.parser = parser;
  }

  @Override
  public T parse(List<String> arguments, Option option) {
    int index = arguments.indexOf("-" + option.value());
    String value = arguments.get(index + 1);
    return parser.apply(value);
  }

}
