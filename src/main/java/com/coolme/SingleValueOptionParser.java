package com.coolme;

import java.util.List;
import java.util.function.Function;

public class SingleValueOptionParser<T> implements OptionParser {

  Function<String, T> valueParser;


  public SingleValueOptionParser(Function<String, T> valueParser) {
    this.valueParser = valueParser;
  }

  @Override
  public T parse(List<String> arguments, String optionValue) {
    int index = arguments.indexOf("-" + optionValue);
    String value = arguments.get(index + 1);
    return parseValue(value);
  }

  private T parseValue(String value) {
    return valueParser.apply(value);
  }

}
