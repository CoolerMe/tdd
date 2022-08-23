package com.coolme.di;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

public class Context {

    private final Map<Class<?>, Provider<?>> providers = new HashMap<>();

    public <Type> void bind(Class<Type> type, Type instance) {
        providers.put(type, () -> instance);
    }

    public <Type, Implementation extends Type>
    void bind(Class<Type> type, Class<Implementation> implementation) {
        providers.put(type, () -> {
            try {
                Constructor<Implementation> constructor = getConstructor(implementation);
                Object[] objects = stream(constructor.getParameters())
                        .map(it -> get(it.getType()))
                        .toArray();
                return constructor.newInstance(objects);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static <Type> Constructor<Type> getConstructor(Class<Type> implementation) {
        Stream<Constructor<?>> constructorStream = stream(implementation.getConstructors())
                .filter(it -> it.isAnnotationPresent(Inject.class));

        return (Constructor<Type>) constructorStream
                .findFirst()
                .orElseGet(() -> {
                    try {
                        return implementation.getConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                });


    }

    public <Type> Type get(Class<Type> type) {
        return (Type) providers.get(type).get();
    }


}
