package com.coolme.di;

import jakarta.inject.Inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public class ContextConfig {


    private final Map<Class<?>, Provider<?>> providers = new HashMap<>();
    private final Map<Class<?>, List<Class<?>>> dependencies = new HashMap<>();

    public <Type> void bind(Class<Type> type, Type instance) {
        providers.put(type, (context) -> instance);
        dependencies.put(type, new ArrayList<>());
    }

    public <Type, Implementation extends Type> void bind(Class<Type> type, Class<Implementation> implementation) {
        Constructor<Implementation> constructor = getConstructor(implementation);
        providers.put(type, new InjectionProvider<>(constructor, type));
        List<Class<?>> dependencyList = stream(constructor.getParameters())
                .map(Parameter::getType)
                .collect(Collectors.toList());
        dependencies.put(type, dependencyList);
    }

    public Context getContext() {

        for (Class<?> component : dependencies.keySet()) {
            for (Class<?> dependency : dependencies.get(component)) {
                if (!dependencies.containsKey(dependency)) {
                    throw new DependencyNotFoundException(component, dependency);
                }
            }
        }

        return new Context() {
            @Override
            public <Type> Optional<Type> get(Class<Type> type) {
                return Optional.ofNullable(providers.get(type))
                        .map(it -> (Type) it.get(this));
            }
        };
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

    interface Provider<Type> {
        Type get(Context context);
    }

    class InjectionProvider<Type> implements Provider<Type> {

        private final Constructor<Type> constructor;

        private final Class<?> componentClass;

        private boolean constructing = false;

        public InjectionProvider(Constructor<Type> constructor, Class<?> componentClass) {
            this.constructor = constructor;
            this.componentClass = componentClass;
        }


        @Override
        public Type get(Context context) {
            if (constructing) {
                throw new CyclicDependencyException(componentClass);
            }

            try {
                constructing = true;
                Object[] objects = stream(constructor.getParameters())
                        .map(it -> context.get(it.getType())
                                .orElseThrow(
                                        () -> new DependencyNotFoundException(componentClass, it.getType())))
                        .toArray();
                return constructor.newInstance(objects);
            } catch (CyclicDependencyException e) {
                throw new CyclicDependencyException(componentClass, e);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } finally {
                constructing = false;
            }
        }
    }

}
