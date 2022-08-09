package com.coolme.tdd;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArgsTest {

  // single
  @Test
  public void should_set_boolean_option_to_true_if_flag_present() {
    BooleanOption option = Args.parse(BooleanOption.class, "-l");
    Assertions.assertTrue(option.logging);
  }

  @Test
  public void should_set_boolean_option_to_false_if_flag_not_present() {
    BooleanOption option = Args.parse(BooleanOption.class);
    Assertions.assertFalse(option.logging);
  }


  // string -d /usr/log
  @Test
  public void should_set_string_option_if_flag_present() {
    StringOption option = Args.parse(StringOption.class, "-d", "/usr/logs");
    Assertions.assertEquals(option.directory, "/usr/logs");
  }

  @Test
  public void should_parse_int_if_flag_present() {
    IntOption option = Args.parse(IntOption.class, "-p", "8080");
    Assertions.assertEquals(option.port, 8080);
  }

  // multi
  @Test
  public void should_parse_multi_options() {
    MultiOption option = Args.parse(MultiOption.class, "-l", "-p", "8080", "-d", "/usr/log");
    Assertions.assertEquals(option.port, 8080);
    Assertions.assertEquals(option.directory, "/usr/log");
    Assertions.assertTrue(option.logging);
  }
  // sad path
  // TODO boolean -l 1 | -l t f
  // TODO string -d | -d /usr/log /usr/conf
  // TODO int -p | -p 8080 8081
  // default value
  // TODO boolean : false
  // TODO int 0
  // TODO string ""

  public record MultiOption(@Option("l") boolean logging, @Option("d") String directory,
                            @Option("p") int port) {

  }

  public record BooleanOption(@Option("l") boolean logging) {

  }

  public record StringOption(@Option("d") String directory) {

  }

  public record IntOption(@Option("p") int port) {


  }
}
