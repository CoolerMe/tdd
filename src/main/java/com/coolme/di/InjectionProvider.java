package com.coolme.di;

import jakarta.inject.Inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

class InjectionProvider<Type> implements Provider<Type> {

    private final Constructor<Type> constructor;

    public InjectionProvider(Class<Type> implementation) {
        this.constructor = getConstructor(implementation);
    }

    @Override
    public Type get(Context context) {
        try {
            Object[] objects = stream(constructor.getParameters())
                    .map(it -> context.get(it.getType()).get())
                    .toArray();
            return constructor.newInstance(objects);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Class<?>> getDependencies() {
        return stream(constructor.getParameters())
                .map(Parameter::getType)
                .collect(Collectors.toList());
    }

    private static <Type> Constructor<Type> getConstructor(Class<Type> implementation) {
        List<Constructor<?>> injectConstructors = stream(implementation.getConstructors())
                .filter(it -> it.isAnnotationPresent(Inject.class))
                .toList();

        if (injectConstructors.size() > 1) {
            throw new MultiInjectConstructorsException();
        }

        return (Constructor<Type>) injectConstructors.stream()
                .findFirst()
                .orElseGet(() -> {
                    try {
                        return implementation.getConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new IllegalComponentException();
                    }
                });
    }

}
