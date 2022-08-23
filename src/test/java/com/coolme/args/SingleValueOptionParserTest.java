package com.coolme.args;

import static com.coolme.args.BooleanParserTest.option;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class SingleValueOptionParserTest {

  @Test
  public void should_not_accept_extra_arguments_for_single_valued_option() {
    ToManyArgumentsException exception = Assertions.assertThrows(ToManyArgumentsException.class,
        () -> new SingleValueOptionParser<>((Integer) 0, Integer::parseInt)
            .parse(List.of("-p", "8080", "8081"), option("p")));

    Assertions.assertEquals("p", exception.getOption());
  }


  @ParameterizedTest
  @ValueSource(strings = {"-p -l", "-p"})
  public void should_not_accept_insufficient_argument_for_single_value(String arguments) {
    InsufficientArgumentsException exception = Assertions.assertThrows(
        InsufficientArgumentsException.class,
        () -> new SingleValueOptionParser<Integer>((Integer) 0, Integer::parseInt)
            .parse(List.of(arguments.split(" ")), option("p")));

    Assertions.assertEquals("p", exception.getOption());
  }

  @Test
  public void should_set_default_value_to_0_for_int_option() {

    int value = new SingleValueOptionParser<Integer>((Integer) 0, Integer::valueOf)
        .parse(List.of(), option("-p"));

    Assertions.assertEquals(0, value);
  }
}
