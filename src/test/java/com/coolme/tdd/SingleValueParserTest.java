package com.coolme.tdd;

import static com.coolme.tdd.BooleanOptionParserTest.option;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SingleValueParserTest {

  // Happy path
  @Test
  public void should_set_string_option_if_flag_present() {
    String result = new SingleValueParser<>("", String::valueOf).parse(
        Arrays.asList("-d", "/usr/log"), option("d"));
    Assertions.assertEquals(result, "/usr/log");
  }


  @Test
  public void should_parse_int_if_flag_present() {
    Object parsed = new Object();
    Function<String, Object> parse = (it) -> parsed;
    Object whatever = new Object();

    Object result = new SingleValueParser<>(whatever, parse).parse(
        Arrays.asList("-p", "8080"), option("p"));
    Assertions.assertEquals(result, parsed);
  }

  // sad path
  //  -d /usr/log /usr/conf
  @Test
  public void should_not_accept_extra_arguments_for_singled_value_option() {
    TooManyArgumentsException exception = Assertions.assertThrows(TooManyArgumentsException.class,
        () -> new SingleValueParser<String>("", String::valueOf).parse(
            Arrays.asList("-d", "/usr/log", "/usr/conf"), option("d")));

    Assertions.assertEquals("d", exception.getValue());
  }

  //  -d | -d -l
  @ParameterizedTest
  @ValueSource(strings = {"-d -l", "-d"})
  public void should_not_accept_insufficient_arguments_for_singled_value_option(String value) {
    InsufficientArgumentsException exception = Assertions.assertThrows(
        InsufficientArgumentsException.class,
        () -> new SingleValueParser<>("", String::valueOf).parse(List.of(value.split(" ")),
            option("d")));

    Assertions.assertEquals("d", exception.getValue());
  }

  // default value
  //  ""
  @Test
  public void should_set_default_value_if_not_option_present() {
    Function<String, Object> parse = (it) -> null;
    Object defaultValue = new Object();

    Object result = new SingleValueParser<>(defaultValue, parse).parse(List.of(), option("d"));
    Assertions.assertSame(result, defaultValue);
  }

}
