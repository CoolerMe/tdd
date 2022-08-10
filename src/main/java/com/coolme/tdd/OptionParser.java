package com.coolme.tdd;

import java.util.List;

interface OptionParser<T> {

  /**
   * Parse arguments to Option
   *
   * @param arguments List of argument
   * @param option    {@link Option} object
   * @return Object parsed
   */
  T parse(List<String> arguments, Option option);

}
