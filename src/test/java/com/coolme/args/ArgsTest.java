package com.coolme.args;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coolme.args.Args;
import com.coolme.args.Option;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


public class ArgsTest {

  //single
  @Test
  public void should_set_boolean_option_to_true_if_flag_present() {
    BooleanOption option = Args.parse(BooleanOption.class, "-l");

    assertTrue(option.logging);
  }

  @Test
  public void should_set_boolean_option_to_false_if_flag_not_present() {
    BooleanOption option = Args.parse(BooleanOption.class);

    assertFalse(option.logging);
  }


  public static record BooleanOption(@Option("l") boolean logging) {

  }

  @Test
  public void should_parse_int_as_option_value() {
    IntOption option = Args.parse(IntOption.class, "-p", "100");

    assertEquals(100, option.port);
  }

  @Test
  @Disabled
  public void should_parse_int_as_option_value_not_present() {
    IntOption option = Args.parse(IntOption.class, "-p");

    assertEquals(0, option.port);
  }

  public static record IntOption(@Option("p") int port) {

  }

  @Test
  public void should_parse_string_as_option() {
    StringOption option = Args.parse(StringOption.class, "-d", "/user/log");

    assertEquals("/user/log", option.directory);
  }

  public static record StringOption(@Option("d") String directory) {

  }

  //multi
  //TODO  -l -p 8080 -d /user/logs
  @Test
  public void parse_multi_options() {
    Options options = Args.parse(Options.class, "-l", "-p", "8080", "-d", "/user/logs");

    assertTrue(options.logging);
    assertEquals(8080, options.port);
    assertEquals("/user/logs", options.directory);
  }
  //sad path
  // TODO  -boolean -l t / -l t f
  // TODO -int -p/ -p 8080 8081
  // TODO -string -d/ -d /user/logs /user/vars
  //default value
  // TODO -boolean false
  //  TODO -int 0
  //  TODO -string ""


  @Test
  @Disabled
  public void should_example_2() {
    ListOptions listOptions = Args.parse(ListOptions.class, "-g", "this", "is", "a", "list", "-d",
        "1", "2", "3", "4");

    assertArrayEquals(new String[]{"this", "is", "a", "list"}, listOptions.group);
    assertArrayEquals(new int[]{1, 2, 3, 4}, listOptions.decimals);
  }

  public static record Options(@Option("l") boolean logging, @Option("p") int port,
                               @Option("d") String directory) {

  }

  public static record ListOptions(@Option("g") String[] group, @Option("d") int[] decimals) {


  }
}
