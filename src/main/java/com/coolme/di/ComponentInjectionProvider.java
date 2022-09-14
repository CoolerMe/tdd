package com.coolme.di;

import jakarta.inject.Inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ComponentInjectionProvider<T> implements ComponentProvider<T> {
    private final Constructor<T> constructor;

    private final List<Field> injectFields;

    public ComponentInjectionProvider(Class<T> implementation) {
        this.constructor = getConstructor(implementation);
        this.injectFields = getInjectFields(implementation);
    }

    private List<Field> getInjectFields(Class<T> implementation) {
        List<Field> fields = new ArrayList<>();
        Class<?> component = implementation;
        while (component != Object.class) {
            fields.addAll(Arrays.stream(component.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(Inject.class))
                    .toList());
            component = component.getSuperclass();
        }

        return fields;
    }

    private static <I> Constructor<I> getConstructor(Class<I> implementation) {
        List<Constructor<?>> injectConstructors = Arrays.stream(implementation.getConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Inject.class)).toList();

        if (injectConstructors.size() > 1) {
            throw new IllegalComponentException();
        }
        return (Constructor<I>) injectConstructors.stream()
                .findFirst()
                .orElseGet(() -> {
                    try {
                        return implementation.getDeclaredConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new IllegalComponentException();
                    }
                });

    }

    @Override
    public T get(Context context) {
        try {
            Object[] dependencies = Arrays.stream(constructor.getParameters())
                    .map(parameter -> context.get(parameter.getType()).get())
                    .toArray(Object[]::new);

            T t = constructor.newInstance(dependencies);
            for (Field field : injectFields) {
                field.set(t, context.get(field.getType()).get());
            }
            return t;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Class<?>> getDependencies() {
        Stream<? extends Class<?>> constructorStream = Arrays.stream(constructor.getParameters()).map(Parameter::getType);
        Stream<? extends Class<?>> classStream = injectFields.stream().map(Field::getType);
        return Stream.concat(constructorStream, classStream)
                .collect(Collectors.toList());
    }
}
