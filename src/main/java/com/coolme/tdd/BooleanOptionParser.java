package com.coolme.tdd;

import com.coolme.tdd.Args.OptionParser;
import java.util.List;

class BooleanOptionParser implements OptionParser {

  @Override
  public Object parse(List<String> arguments, Option option) {
    return arguments.contains("-" + option.value());
  }
}
