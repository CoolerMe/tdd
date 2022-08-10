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
    } catch (IllegalOptionException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Object parse(List<String> arguments, Parameter parameter) {
    if (!parameter.isAnnotationPresent(Option.class)) {
      throw new IllegalOptionException(parameter.getName());
    }
    return PARSER.get(parameter.getType()).parse(arguments, parameter.getAnnotation(Option.class));
  }

  public static Map<Class<?>, OptionParser<?>> PARSER = Map.of(
      int.class, new SingleValueParser<>(0, Integer::parseInt),
      String.class, new SingleValueParser<>("", String::valueOf),
      boolean.class, new BooleanOptionParser());

}
