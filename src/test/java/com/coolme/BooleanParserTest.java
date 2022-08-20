package com.coolme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.Annotation;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BooleanParserTest {


  @Test
  public void should_not_accept_extra_argument_for_boolean_value() {
    ToManyArgumentsException exception = Assertions.assertThrows(ToManyArgumentsException.class,
        () -> new BooleanParser()
            .parse(List.of("-l", "t"), option("l")));

    assertEquals("l", exception.getOption());
  }

  @Test
  public void should_set_default_value_to_false_if_not_present() {
    assertEquals(false, new BooleanParser().parse(List.of(), option("l")));
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
