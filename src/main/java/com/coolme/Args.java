package com.coolme;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Args {

  public static <T> T parse(Class<T> optionClass, String... args) {
    Constructor<?>[] constructors = optionClass.getDeclaredConstructors();
    Constructor<?> constructor = constructors[0];

    List<String> arguments = Arrays.asList(args);
    Parameter[] parameters = constructor.getParameters();

    Object[] values = Arrays.stream(parameters)
        .map(parameter -> getValue(parameter, arguments))
        .toArray();

    try {
      return (T) constructor.newInstance(values);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Object getValue(Parameter parameter, List<String> arguments) {

    return PARSER.get(parameter.getType())
        .parse(arguments, parameter.getAnnotation(Option.class).value());
  }

  private static final Map<Class<?>, OptionParser> PARSER = Map.of(int.class,
      new SingleValueOptionParser<>(Integer::parseInt),
      boolean.class, new BooleanParser(),
      String.class, new SingleValueOptionParser<>(String::valueOf));

}
