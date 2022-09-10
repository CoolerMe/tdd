package com.coolme.di;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

public class ContextConfig {

    private final Map<Class<?>, ComponentProvider<?>> componentProviders = new HashMap<>();


    public <T> void bind(Class<T> type, T instance) {
        componentProviders.put(type, context -> instance);

    }

    public <T, I extends T> void bind(Class<T> componentType, Class<I> implementation) {
        componentProviders.put(componentType, new ComponentInjectionProvider<>(implementation));
    }

    public Context getContext() {
        componentProviders.keySet()
                .forEach(component -> checkDependencies(component, new Stack<>()));

        return new Context() {
            @Override
            public <T> Optional<T> get(Class<T> componentClass) {
                return Optional.ofNullable(componentProviders.get(componentClass)).map(provider -> (T) provider.get(this));
            }
        };
    }

    private void checkDependencies(Class<?> componentClass, Stack<Class<?>> visiting) {
        for (Class<?> dependency : componentProviders.get(componentClass).getDependencies()) {

            if (!componentProviders.containsKey(dependency)) {
                throw new DependencyNotFoundException(componentClass, dependency);
            }

            if (visiting.contains(dependency)) {
                throw new CyclicDependenciesException(visiting);
            }
            visiting.push(dependency);
            checkDependencies(dependency, visiting);
            visiting.pop();
        }
    }


}
