package com.coolme.tdd;

import com.coolme.tdd.exception.IllegalOptionException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author coolme
 */
public class Args {

    private static final Map<Class<?>, OptionParser> PARSERS = Map.of(
        int.class, Parsers.unary(0, Integer::parseInt),
        boolean.class, Parsers.bool(),
        String.class, Parsers.unary("", String::valueOf),
        Integer[].class, Parsers.list(Integer[]::new, Integer::parseInt),
        String[].class, Parsers.list(String[]::new, String::valueOf));

    public static <T> T parse(Class<T> optionClass, String... args) {
        try {
            Constructor<?> constructor = optionClass.getDeclaredConstructors()[0];
            List<String> arguments = Arrays.asList(args);

            Object[] values = Arrays.stream(constructor.getParameters())
                .map(it -> parseOption(it, arguments)).toArray();

            return (T) constructor.newInstance(values);
        } catch (IllegalOptionException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object parseOption(Parameter parameter, List<String> arguments) {
        if (!parameter.isAnnotationPresent(Option.class)) {
            throw new IllegalOptionException(parameter.getName());
        }

        return PARSERS.get(parameter.getType())
            .parse(arguments, parameter.getAnnotation(Option.class));
    }


}
