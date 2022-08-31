package com.coolme.di;

import jakarta.inject.Provider;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

public class ContextConfig {

    private final Map<Class<?>, DiProvider<?>> providers = new HashMap<>();

    public <T> void bind(Class<T> type, T instance) {
        providers.put(type, context -> instance);
    }

    public <T, I extends T> void bind(Class<T> type, Class<I> implementation) {
        providers.put(type, new InjectionProvider<>(implementation));
    }


    public Context getContext() {
        providers.keySet()
                .forEach(component -> checkDependencies(component, new Stack<>()));

        return new Context() {

            @Override
            public Optional get(Type type) {
                if (isContainerType(type)) {
                    return getContainer((ParameterizedType) type);
                } else {
                    return getComponent((Class<?>) type);
                }
            }

            private <T> Optional<T> getComponent(Class<T> type) {
                return Optional.ofNullable(providers.get(type))
                        .map(it -> (T) it.get(this));
            }


            private Optional getContainer(ParameterizedType type) {
                if (type.getRawType() != Provider.class) {
                    return Optional.empty();
                }
                Class<?> componentClass = getComponentType(type);
                return Optional.ofNullable(providers.get(componentClass))
                        .map(provider -> (Provider<Object>) (() -> provider.get(this)));
            }


        };
    }

    private static Class<?> getComponentType(Type type) {
        return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
    }

    private static boolean isContainerType(Type type) {
        return type instanceof ParameterizedType;
    }

    private void checkDependencies(Class<?> component, Stack<Class<?>> visiting) {
        for (Type dependency : providers.get(component).getDependencyTypes()) {
            if (dependency instanceof Class<?>) {
                checkComponentTypeDependencies(component, visiting, (Class<?>) dependency);
            }

            if (isContainerType(dependency)) {
                checkContainerTypeDependencies(component, dependency);
            }
        }
    }

    private void checkContainerTypeDependencies(Class<?> component, Type dependency) {
        Class<?> type = getComponentType(dependency);
        if (!providers.containsKey(type)) {
            throw new DependencyNotFoundException(component, type);
        }
    }


    private void checkComponentTypeDependencies(Class<?> component, Stack<Class<?>> visiting, Class<?> dependency) {
        if (!providers.containsKey(dependency)) {
            throw new DependencyNotFoundException(component, dependency);
        }

        if (visiting.contains(dependency)) {
            throw new CyclicDependencyException(visiting);
        }
        visiting.push(dependency);
        checkDependencies(dependency, visiting);
        visiting.pop();

    }


}
