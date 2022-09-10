package com.coolme.di;

import jakarta.inject.Inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class ComponentInjectionProvider<T> implements ComponentProvider<T> {
    private final Constructor<T> constructor;

    public ComponentInjectionProvider(Class<T> implementation) {
        this.constructor = getConstructor(implementation);
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
                        return implementation.getConstructor();
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
            return constructor.newInstance(dependencies);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Class<?>> getDependencies() {
        return Arrays.stream(constructor.getParameters()).map(Parameter::getType)
                .collect(Collectors.toList());
    }
}
