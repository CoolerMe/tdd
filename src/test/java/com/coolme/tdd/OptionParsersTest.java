package com.coolme.tdd;

import static com.coolme.tdd.OptionParsersTest.BooleanParser.option;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OptionParsersTest {

  @Nested
  class UnaryParser {

    // Happy path
    @Test
    public void should_set_string_option_if_flag_present() {
      String result = OptionParsers.unary("", String::valueOf).parse(
          Arrays.asList("-d", "/usr/log"), option("d"));
      Assertions.assertEquals(result, "/usr/log");
    }


    @Test
    public void should_parse_int_if_flag_present() {
      Object parsed = new Object();
      Function<String, Object> parse = (it) -> parsed;
      Object whatever = new Object();

      Object result = OptionParsers.unary(whatever, parse).parse(
          Arrays.asList("-p", "8080"), option("p"));
      Assertions.assertEquals(result, parsed);
    }

    // sad path
    //  -d /usr/log /usr/conf
    @Test
    public void should_not_accept_extra_arguments_for_singled_value_option() {
      TooManyArgumentsException exception = Assertions.assertThrows(TooManyArgumentsException.class,
          () -> OptionParsers.unary("", String::valueOf).parse(
              Arrays.asList("-d", "/usr/log", "/usr/conf"), option("d")));

      Assertions.assertEquals("d", exception.getValue());
    }

    //  -d | -d -l
    @ParameterizedTest
    @ValueSource(strings = {"-d -l", "-d"})
    public void should_not_accept_insufficient_arguments_for_singled_value_option(String value) {
      InsufficientArgumentsException exception = Assertions.assertThrows(
          InsufficientArgumentsException.class,
          () -> OptionParsers.unary("", String::valueOf).parse(List.of(value.split(" ")),
              option("d")));

      Assertions.assertEquals("d", exception.getValue());
    }

    // default value
    //  ""
    @Test
    public void should_set_default_value_if_not_option_present() {
      Function<String, Object> parse = (it) -> null;
      Object defaultValue = new Object();

      Object result = OptionParsers.unary(defaultValue, parse)
          .parse(List.of(), option("d"));
      Assertions.assertSame(result, defaultValue);
    }
  }

  @Nested
  class BooleanParser {

    // sad path
    //  boolean -l 1 | -l t f
    @Test
    public void should_not_accept_extra_arguments() {
      TooManyArgumentsException exception = Assertions.assertThrows(
          TooManyArgumentsException.class,
          () -> OptionParsers.bool().parse(Arrays.asList("-l", "t"), option("l")));

      Assertions.assertEquals(exception.getValue(), "l");
    }

    // Happy path
    @Test
    public void should_set_boolean_option_to_true_if_flag_present() {
      boolean logging = OptionParsers.bool().parse(List.of("-l"), option("l"));
      Assertions.assertTrue(logging);
    }

    // default value
    @Test
    public void should_set_default_value_as_false_if_not_present() {
      boolean logging = OptionParsers.bool().parse(List.of(), option("l"));
      Assertions.assertFalse(logging);
    }


    static Option option(String value) {
      return new Option() {

        @Override
        public Class<? extends Annotation> annotationType() {
          return Option.class;
        }

        @Override
        public String value() {
          return value;
        }
      };
    }

  }
}
