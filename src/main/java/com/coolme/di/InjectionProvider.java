package com.coolme.di;

import jakarta.inject.Inject;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;

class InjectionProvider<T> implements DiProvider<T> {

    private final Constructor<T> constructor;
    private final List<Field> fields;
    private final List<Method> methods;

    public InjectionProvider(Class<T> implementation) {
        this.constructor = getConstructor(implementation);
        this.fields = getFields(implementation);
        this.methods = getMethods(implementation);

        checkFinalField();
        checkGenericMethod();
    }


    @Override
    public T get(Context context) {
        try {
            Object[] objects = toDependencies(context, constructor);
            T instance = constructor.newInstance(objects);

            for (Field field : fields) {
                field.set(instance, toDependency(context, field));
            }

            for (Method method : methods) {
                method.invoke(instance, toDependencies(context, method));
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public List<Class<?>> getDependencies() {
        return concat(methods.stream().flatMap(method -> stream(method.getParameterTypes())),
                concat(stream(constructor.getParameters()).map(Parameter::getType),
                        fields.stream().map(Field::getType)
                ))
                .toList();
    }

    @Override
    public List<java.lang.reflect.Type> getDependencyTypes() {
        return concat(concat(stream(constructor.getParameters()).map(Parameter::getParameterizedType)
                        , fields.stream().map(Field::getGenericType)),
                methods.stream().flatMap(m -> stream(m.getParameters()).map(Parameter::getParameterizedType)))
                .toList();
    }

    private void checkGenericMethod() {
        boolean genericMethodFound = methods.stream()
                .anyMatch(method -> method.getTypeParameters().length != 0);

        if (genericMethodFound) {
            throw new IllegalComponentException();
        }
    }

    private void checkFinalField() {
        boolean finalFieldFound = fields.stream()
                .anyMatch(field -> Modifier.isFinal(field.getModifiers()));

        if (finalFieldFound) {
            throw new IllegalComponentException();
        }
    }

    private static <Type> Constructor<Type> getConstructor(Class<Type> implementation) {
        if (Modifier.isAbstract(implementation.getModifiers())) {
            throw new IllegalComponentException();
        }

        List<Constructor<?>> injectConstructors = injectables(implementation.getConstructors()).toList();

        if (injectConstructors.size() > 1) {
            throw new MultiInjectConstructorsException();
        }

        return (Constructor<Type>) injectConstructors.stream()
                .findFirst()
                .orElseGet(() -> defaultConstructor(implementation));
    }


    private static <Type> Constructor<Type> defaultConstructor(Class<Type> implementation) {
        try {
            return implementation.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalComponentException();
        }
    }


    private static Stream<Method> getMethodStream(Class<?> current) {
        return injectables(current.getDeclaredMethods());
    }

    private static List<Field> getFields(Class<?> implementation) {
        return traverse(implementation, (fields, current) -> injectables(current.getDeclaredFields()).toList());
    }

    private static <T> List<T> traverse(Class<?> implementation, BiFunction<List<T>, Class<?>, List<T>> finder) {
        List<T> members = new ArrayList<>();

        Class<?> current = implementation;
        while (current != Object.class) {
            members.addAll(finder.apply(members, current));
            current = current.getSuperclass();
        }

        return members;
    }

    private static List<Method> getMethods(Class<?> implementation) {
        List<Method> list = traverse(implementation, (methods, current) -> getMethodStream(current)
                .filter(method -> isOverrideByInjectMethod(methods, method))
                .filter(method -> isOverrideByNoInjectMethod(implementation, method))
                .toList());

        Collections.reverse(list);
        return list;
    }

    private static <T extends AnnotatedElement> Stream<T> injectables(T[] elements) {
        return stream(elements)
                .filter(it -> it.isAnnotationPresent(Inject.class));
    }

    private static boolean isOverrideByNoInjectMethod(Class<?> implementation, Method subMethod) {
        return stream(implementation.getDeclaredMethods())
                .filter(method -> !method.isAnnotationPresent(Inject.class))
                .noneMatch(superMethod -> isOverride(subMethod, superMethod));
    }

    private static boolean isOverrideByInjectMethod(List<Method> methods, Method method) {
        return methods.stream()
                .noneMatch(superMethod -> isOverride(method, superMethod));
    }

    private static boolean isOverride(Method source, Method target) {
        return target.getName().equals(source.getName())
                && Arrays.equals(target.getParameters(), source.getParameters());
    }

    private static Object toDependency(Context context, Field field) {
        Type type = field.getGenericType();
        return toDependency(context, type);
    }

    private static Object toDependency(Context context, Type type) {
        return context.getType(type).get();
    }


    private static Object[] toDependencies(Context context, Executable executable) {
        return stream(executable.getParameters())
                .map(p -> {
                    Type type = p.getParameterizedType();
                    return toDependency(context, type);
                }).toArray(Object[]::new);
    }
}
