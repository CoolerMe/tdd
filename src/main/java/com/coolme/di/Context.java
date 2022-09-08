package com.coolme.di;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Context {

    private final Map<Class<?>, Provider<?>> providers = new HashMap<>();

    public <T> void bind(Class<T> type, T instance) {
        providers.put(type, () -> instance);
    }


    public <T, I extends T> void bind(Class<T> componentType, Class<I> implementation) {
        Constructor<I> constructor = getConstructor(implementation);

        providers.put(componentType, (Provider<I>) () -> {
            try {

                Object[] dependencies = Arrays.stream(constructor.getParameters())
                        .map(parameter -> get(parameter.getType()).orElseThrow(DependencyNotFoundException::new))
                        .toArray(Object[]::new);
                return (I) constructor.newInstance(dependencies);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
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

    public <T> Optional<T> get(Class<T> componentClass) {
        return Optional.ofNullable(providers.get(componentClass)).map(provider -> (T) provider.get());
    }

}
