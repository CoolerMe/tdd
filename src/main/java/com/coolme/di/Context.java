package com.coolme.di;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Context {

    private final Map<Class<?>, Provider<?>> providers = new HashMap<>();

    public <T> void bind(Class<T> type, T instance) {
        providers.put(type, () -> instance);
    }

    public <T> T get(Class<T> type) {
        return (T) providers.get(type).get();
    }

    public <T, I extends T> void bind(Class<T> componentType, Class<I> implementation) {
        Constructor<?>[] injectConstructors = Arrays.stream(implementation.getConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                .toArray(Constructor<?>[]::new);

        if (injectConstructors.length > 1) {
            throw new IllegalComponentException();
        }

        if (injectConstructors.length == 0 && Arrays.stream(implementation.getConstructors())
                .anyMatch(constructor -> constructor.getParameters().length > 0)) {
            throw new IllegalComponentException();
        }

        providers.put(componentType, (Provider<I>) () -> {
            try {
                Constructor<I> constructor = getConstructor(implementation);
                Object[] dependencies = Arrays.stream(constructor.getParameters())
                        .map(parameter -> get(parameter.getType()))
                        .toArray(Object[]::new);
                return (I) constructor.newInstance(dependencies);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static <I> Constructor<I> getConstructor(Class<I> implementation) {
        return (Constructor<I>) Arrays.stream(implementation.getConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                .findFirst()
                .orElseGet(() -> {
                    try {
                        return implementation.getConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                });

    }
}
