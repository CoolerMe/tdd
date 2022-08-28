package com.coolme.di;

import jakarta.inject.Inject;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

class InjectionProvider<Type> implements Provider<Type> {

    private final Constructor<Type> constructor;
    private final List<Field> fields;
    private final List<Method> methods;

    public InjectionProvider(Class<Type> implementation) {
        this.constructor = getConstructor(implementation);
        this.fields = getFields(implementation);
        this.methods = getMethods(implementation);

    }


    @Override
    public Type get(Context context) {
        try {
            Object[] objects = stream(constructor.getParameters())
                    .map(it -> context.get(it.getType()).get())
                    .toArray();
            Type instance = constructor.newInstance(objects);
            for (Field field : fields) {
                field.set(instance, context.get(field.getType()).get());
            }

            for (Method method : methods) {
                Object[] args = stream(method.getParameters())
                        .map(parameter -> context.get(parameter.getType()).get()).toArray();
                method.invoke(instance, args);
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Class<?>> getDependencies() {
        return Stream.concat(methods.stream().<Class<?>>
                                flatMap(method -> stream(method.getParameters())
                                .map(Parameter::getType)),
                        Stream.concat(stream(constructor.getParameters()).map(Parameter::getType),
                                fields.stream().map(Field::getType)
                        ))
                .toList();
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
                        return implementation.getDeclaredConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new IllegalComponentException();
                    }
                });
    }

    private static List<Method> getMethods(Class<?> implementation) {

        List<Method> methodList = new ArrayList<>();
        Class<?> current = implementation;
        while (current != Object.class) {
            methodList.addAll(Stream.of(current.getDeclaredMethods())
                    .filter(it -> it.isAnnotationPresent(Inject.class))
                    .filter(newMethod -> methodList.stream()
                            .noneMatch(oldMethod -> oldMethod.getName().equals(newMethod.getName())
                                    && Arrays.equals(oldMethod.getParameters(), newMethod.getParameters())
                            ))
                    .filter(newMethod -> stream(implementation.getDeclaredMethods())
                            .filter(m2 -> !m2.isAnnotationPresent(Inject.class))
                            .noneMatch(oldMethod -> oldMethod.getName().equals(newMethod.getName())
                                    && Arrays.equals(oldMethod.getParameters(), newMethod.getParameters())))

                    .toList());
            current = current.getSuperclass();
        }
        Collections.reverse(methodList);

        return methodList;
    }

    private static List<Field> getFields(Class<?> implementation) {
        List<Field> fieldList = new ArrayList<>();

        Class<?> current = implementation;
        while (current != Object.class) {
            fieldList.addAll(stream(current.getDeclaredFields())
                    .filter(it -> it.isAnnotationPresent(Inject.class))
                    .toList());
            current = current.getSuperclass();
        }

        return fieldList;
    }

}
