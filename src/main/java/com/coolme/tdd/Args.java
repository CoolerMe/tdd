package com.coolme.tdd;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author coolme
 */
public class Args {

  public static <T> T parse(Class<T> clazz, String... args) {

    try {
      Constructor<?>[] constructors = clazz.getDeclaredConstructors();
      List<String> arguments = Arrays.asList(args);
      Parameter[] parameters = constructors[0].getParameters();

      Object[] values = Arrays.stream(parameters).map(parameter -> OptionParsers.parse(arguments, parameter))
          .toArray();
      return (T) constructors[0].newInstance(values);
    } catch (IllegalOptionException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Map<Class<?>, OptionParser<?>> PARSER = Map.of(
      int.class, OptionParsers.unary(0, Integer::parseInt),
      String.class, OptionParsers.unary("", String::valueOf),
      boolean.class, OptionParsers.bool());

}
