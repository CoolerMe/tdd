package com.coolme.tdd;

import com.coolme.tdd.exception.IllegalOptionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ArgsTest {

  // multi
  @Test
  public void should_parse_multi_options() {
    MultiOption option = Args.parse(MultiOption.class, "-l", "-p", "8080", "-d", "/usr/log");

    Assertions.assertEquals(option.port, 8080);
    Assertions.assertEquals(option.directory, "/usr/log");
    Assertions.assertTrue(option.logging);
  }

  @Test
  public void should_parse_list_options() {
    ListOption option = Args.parse(ListOption.class, "-d", "-1", "2", "3", "4", "-g", "this", "is",
        "a", "cat");

    Assertions.assertArrayEquals(new Integer[]{-1, 2, 3, 4}, option.decimals);
    Assertions.assertArrayEquals(new String[]{"this", "is", "a", "cat"}, option.group);
  }

  @Test
  public void should_throw_illegal_option_exception_if_no_annotation_present() {
    IllegalOptionException exception = Assertions.assertThrows(
        IllegalOptionException.class,
        () -> Args.parse(MultiOptionWithoutAnnotation.class, "-l", "-p", "8080", "-d", "/usr/log"));

    Assertions.assertEquals("port", exception.getParameter());
  }


  public record MultiOption(@Option("l") boolean logging, @Option("d") String directory,
                            @Option("p") int port) {

  }

  public record MultiOptionWithoutAnnotation(@Option("l") boolean logging,
                                             @Option("d") String directory,
                                             int port) {

  }

  public record ListOption(@Option("d") Integer[] decimals, @Option("g") String[] group) {

  }
}
