package com.coolme.tdd;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArgsTest {


  // multi
  @Test
  public void should_parse_multi_options() {
    MultiOption option = Args.parse(MultiOption.class, "-l", "-p", "8080", "-d", "/usr/log");
    Assertions.assertEquals(option.port, 8080);
    Assertions.assertEquals(option.directory, "/usr/log");
    Assertions.assertTrue(option.logging);
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
}
