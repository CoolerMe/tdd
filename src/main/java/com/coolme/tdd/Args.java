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

    public static Map<Class<?>, OptionParser<?>> PARSER = Map.of(
        int.class, OptionParsers.unary(0, Integer::parseInt),
        String.class, OptionParsers.unary("", String::valueOf),
        boolean.class, OptionParsers.bool(),
        Integer[].class, OptionParsers.list(Integer[]::new, Integer::parseInt),
        String[].class, OptionParsers.list(String[]::new, String::valueOf)
    );


    public static <T> T parse(Class<T> clazz, String... args) {
        return parse(PARSER, args, clazz);
    }

    private static <T> T parse(Map<Class<?>, OptionParser<?>> parser, String[] args,
        Class<T> option) {
        try {
            Constructor<?>[] constructors = option.getDeclaredConstructors();
            List<String> arguments = Arrays.asList(args);
            Parameter[] parameters = constructors[0].getParameters();

            Object[] values = Arrays.stream(parameters)
                .map(parameter -> parse(arguments, parameter, parser))
                .toArray();
            return (T) constructors[0].newInstance(values);
        } catch (IllegalOptionException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object parse(List<String> arguments, Parameter parameter,
        Map<Class<?>, OptionParser<?>> parser) {
        if (!parameter.isAnnotationPresent(Option.class)) {
            throw new IllegalOptionException(parameter.getName());
        }

        return parser.get(parameter.getType())
            .parse(arguments, parameter.getAnnotation(Option.class));
    }
}
