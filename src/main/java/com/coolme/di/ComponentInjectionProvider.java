package com.coolme.di;

import jakarta.inject.Inject;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.*;

class ComponentInjectionProvider<T> implements ComponentProvider<T> {
    private final Constructor<T> constructor;

    private final List<Field> injectFields;

    private final List<Method> injectMethods;

    public ComponentInjectionProvider(Class<T> implementation) {
        this.constructor = getConstructor(implementation);
        this.injectFields = getInjectFields(implementation);
        this.injectMethods = getInjectMethods(implementation);
    }

    private List<Method> getInjectMethods(Class<T> implementation) {
        List<Method> methods = new ArrayList<>();
        Class<?> current = implementation;
        while (current != Object.class) {
            methods.addAll(
                    Arrays.stream(current.getDeclaredMethods())
                            .filter(method -> method.isAnnotationPresent(Inject.class))
                            .filter(method -> methods.stream().noneMatch(item -> method.getName().equals(item.getName())
                                    && Arrays.equals(item.getParameterTypes(), method.getParameterTypes())))
                            .filter(method -> Arrays.stream(implementation.getDeclaredMethods())
                                    .filter(method1 -> !method1.isAnnotationPresent(Inject.class))
                                    .noneMatch(item -> method.getName().equals(item.getName())
                                            && Arrays.equals(item.getParameterTypes(), method.getParameterTypes())))
                            .toList());
            current = current.getSuperclass();
        }
        Collections.reverse(methods);

        return methods;
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

            for (Method method : injectMethods) {
                method.setAccessible(true);
                method.invoke(t, Arrays.stream(method.getParameterTypes())
                        .map(type -> context.get(type).get())
                        .toArray(Object[]::new));
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
        Stream<Class<?>> stream = injectMethods.stream()
                .flatMap(method -> Arrays.stream(method.getParameterTypes()));
        return Stream.concat(stream, concat(constructorStream, classStream))
                .collect(Collectors.toList());
    }
}
