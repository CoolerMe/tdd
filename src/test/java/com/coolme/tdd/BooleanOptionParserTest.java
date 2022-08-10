package com.coolme.tdd;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BooleanOptionParserTest {

  // sad path
  //  boolean -l 1 | -l t f
  @Test
  public void should_not_accept_extra_arguments() {
    TooManyArgumentsException exception = Assertions.assertThrows(
        TooManyArgumentsException.class,
        () -> new BooleanOptionParser().parse(Arrays.asList("-l", "t"), option("l")));

    Assertions.assertEquals(exception.getValue(), "l");
  }

  // Happy path
  @Test
  public void should_set_boolean_option_to_true_if_flag_present() {
    boolean logging = new BooleanOptionParser().parse(List.of("-l"), option("l"));
    Assertions.assertTrue(logging);
  }

  // default value
  // TODO int 0
  @Test
  public void should_set_default_value_as_false_if_not_present() {
    boolean logging = new BooleanOptionParser().parse(List.of(), option("l"));
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
