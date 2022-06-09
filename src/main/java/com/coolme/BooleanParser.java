package com.coolme;

import java.util.List;

public class BooleanParser implements OptionParser {

  @Override
  public Object parse(List<String> arguments, String optionValue) {
    return arguments.contains("-" + optionValue);
  }
}
