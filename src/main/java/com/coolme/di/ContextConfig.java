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
                    return get((ParameterizedType) type);
                } else {
                    return get((Class<?>) type);
                }
            }

            private <T> Optional<T> get(Class<T> type) {
                return Optional.ofNullable(providers.get(type))
                        .map(it -> (T) it.get(this));
            }


            private Optional get(ParameterizedType type) {
                if (type.getRawType() != Provider.class) {
                    return Optional.empty();
                }
                Class<?> componentClass = (Class<?>) type.getActualTypeArguments()[0];
                return Optional.ofNullable(providers.get(componentClass))
                        .map(provider -> (Provider<Object>) (() -> provider.get(this)));
            }


        };
    }

    private static boolean isContainerType(Type type) {
        return type instanceof ParameterizedType;
    }

    private void checkDependencies(Class<?> component, Stack<Class<?>> visiting) {
        for (Type dependency : providers.get(component).getDependencyTypes()) {
            if (dependency instanceof Class<?>) {
                checkDependencies(component, visiting, (Class<?>) dependency);
            }

            if (isContainerType(dependency)) {
                Class<?> type = (Class<?>) ((ParameterizedType) dependency).getActualTypeArguments()[0];
                if (!providers.containsKey(type)) {
                    throw new DependencyNotFoundException(component, type);
                }
            }
        }
    }


    private void checkDependencies(Class<?> component, Stack<Class<?>> visiting, Class<?> dependency) {
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
