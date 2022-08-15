package com.coolme.tdd;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author coolme
 */
public class Args {

    private static final Map<Class<?>, OptionParser> PARSERS = Map.of(
        int.class, new SingleValuedOption(Integer::parseInt),
        boolean.class, new BooleanOptionParser(),
        String.class, new SingleValuedOption(String::valueOf));

    public static <T> T parseBoolean(Class<T> optionClass, String... args) {
        try {
            Constructor<?> constructor = optionClass.getDeclaredConstructors()[0];
            List<String> arguments = Arrays.asList(args);

            Object[] values = Arrays.stream(constructor.getParameters())
                .map(it -> parseOption(it, arguments)).toArray();

            return (T) constructor.newInstance(values);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object parseOption(Parameter parameter, List<String> arguments) {
        return PARSERS.get(parameter.getType())
            .parse(arguments, parameter.getAnnotation(Option.class));
    }


}
