package com.coolme.di;

import jakarta.inject.Provider;

import java.lang.reflect.ParameterizedType;
import java.util.*;

public class ContextConfig {

    private final Map<Class<?>, DiProvider<?>> providers = new HashMap<>();

    public <Type> void bind(Class<Type> type, Type instance) {
        providers.put(type, new DiProvider<Type>() {
            @Override
            public Type get(Context context) {
                return instance;
            }

            @Override
            public List<Class<?>> getDependencies() {
                return List.of();
            }
        });
    }

    public <Type, Implementation extends Type> void bind(Class<Type> type, Class<Implementation> implementation) {
        providers.put(type, new InjectionProvider<>(implementation));
    }


    public Context getContext() {
        providers.keySet()
                .forEach(component -> checkDependencies(component, new Stack<>()));

        return new Context() {
            @Override
            public <Type> Optional<Type> get(Class<Type> type) {
                return Optional.ofNullable(providers.get(type))
                        .map(it -> (Type) it.get(this));
            }

            @Override
            public Optional get(ParameterizedType type) {
                if (type.getRawType() != Provider.class) {
                    return Optional.empty();
                }
                Class<?> componentClass = (Class<?>) type.getActualTypeArguments()[0];
                return Optional.ofNullable(providers.get(componentClass))
                        .map(provider -> (Provider<Object>) (() -> provider.get(this)));

            }
        };
    }

    private void checkDependencies(Class<?> component, Stack<Class<?>> visiting) {
        for (Class<?> dependency : providers.get(component).getDependencies()) {
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


}
