package com.coolme.tdd;

import java.util.List;

class BooleanOptionParser implements OptionParser<Boolean> {

  @Override
  public Boolean parse(List<String> arguments, Option option) {

    return SingleValueParser.values(arguments, option, 0)
        .map(it -> true)
        .orElse(false);
  }
}
