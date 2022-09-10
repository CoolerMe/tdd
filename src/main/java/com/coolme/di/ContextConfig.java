package com.coolme.di;

import jakarta.inject.Inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

public class ContextConfig {

    private final Map<Class<?>, ComponentProvider<?>> componentProviders = new HashMap<>();

    private final Map<Class<?>, List<Class<?>>> dependencies = new HashMap<>();

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

    public <T> void bind(Class<T> type, T instance) {
        componentProviders.put(type, context -> instance);
        dependencies.put(type, List.of());
    }

    public <T, I extends T> void bind(Class<T> componentType, Class<I> implementation) {
        Constructor<I> constructor = getConstructor(implementation);
        componentProviders.put(componentType, new ComponentInjectionProvider<>(constructor, componentType));
        dependencies.put(componentType, Arrays.stream(constructor.getParameters()).map(Parameter::getType)
                .collect(Collectors.toList()));
    }

    public Context getContext() {

        for (Class<?> component : dependencies.keySet()) {
            for (Class<?> dependency : dependencies.get(component)) {
                if (!componentProviders.containsKey(dependency)) {
                    throw new DependencyNotFoundException(component, dependency);
                }
            }
        }

        return new Context() {
            @Override
            public <T> Optional<T> get(Class<T> componentClass) {
                return Optional.ofNullable(componentProviders.get(componentClass)).map(provider -> (T) provider.get(this));
            }
        };
    }

    interface ComponentProvider<T> {
        T get(Context context);
    }

    static class ComponentInjectionProvider<T> implements ComponentProvider<T> {
        private final Constructor<T> constructor;
        private final Class<?> componentType;
        private boolean constructing = false;

        public ComponentInjectionProvider(Constructor<T> constructor, Class<?> componentType) {
            this.constructor = constructor;
            this.componentType = componentType;
        }

        @Override
        public T get(Context context) {
            if (constructing) {
                throw new CyclicDependenciesException(componentType);
            }
            try {
                constructing = true;
                Object[] dependencies = Arrays.stream(constructor.getParameters())
                        .map(parameter -> context.get(parameter.getType())
                                .orElseThrow(() -> new DependencyNotFoundException(componentType, parameter.getType())))
                        .toArray(Object[]::new);
                return constructor.newInstance(dependencies);
            } catch (CyclicDependenciesException e) {
                throw new CyclicDependenciesException(componentType, e);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } finally {
                constructing = false;
            }
        }
    }


}
