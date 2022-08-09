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

      Object[] values = Arrays.stream(parameters).map(parameter -> parse(arguments, parameter))
          .toArray();
      return (T) constructors[0].newInstance(values);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Object parse(List<String> arguments, Parameter parameter) {
    OptionParser parser = PARSER.get(parameter.getType());
    return parser.parse(arguments, parameter.getAnnotation(Option.class));
  }

  public static Map<Class<?>, OptionParser> PARSER = Map.of(
      int.class, new SingleValueParser<>(Integer::parseInt),
      String.class, new SingleValueParser<>(String::valueOf),
      boolean.class, new BooleanOptionParser());

  interface OptionParser {

    /**
     * Parse arguments to Option
     *
     * @param arguments List of argument
     * @param option    {@link Option} object
     * @return Object parsed
     */
    Object parse(List<String> arguments, Option option);

  }

}
