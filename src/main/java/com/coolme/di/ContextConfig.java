package com.coolme.di;

import jakarta.inject.Qualifier;

import java.lang.annotation.Annotation;
import java.util.*;

public class ContextConfig {

    private final Map<Component, Provider<?>> components = new HashMap<>();

    public <T> void bind(Class<T> type, T instance) {
        components.put(new Component(type, null), context -> instance);
    }

    public <T> void bind(Class<T> type, T instance, Annotation... qualifiers) {
        boolean anyMatch = Arrays.stream(qualifiers)
                .anyMatch(qualifier -> !qualifier.annotationType().isAnnotationPresent(Qualifier.class));

        if (anyMatch) {
            throw new IllegalComponentException();
        }
        for (Annotation qualifier : qualifiers) {
            components.put(new Component(type, qualifier), context -> instance);
        }
    }


    public <T, I extends T> void bind(Class<T> type, Class<I> implementation) {
        components.put(new Component(type, null), new InjectionProvider<>(implementation));
    }

    public <T, I extends T> void bind(Class<T> type, Class<I> implementation, Annotation... qualifiers) {
        boolean anyMatch = Arrays.stream(qualifiers)
                .anyMatch(qualifier -> !qualifier.annotationType().isAnnotationPresent(Qualifier.class));

        if (anyMatch) {
            throw new IllegalComponentException();
        }
        for (Annotation qualifier : qualifiers) {
            components.put(new Component(type, qualifier), new InjectionProvider<>(implementation));
        }
    }

    public Context getContext() {
        components.keySet().forEach(component -> checkDependencies(component, new Stack<>()));
        return new Context() {

            @Override
            public <T> Optional<T> get(ComponentRef<T> componentRef) {
                if (componentRef.isContainer()) {
                    if (componentRef.getContainer() != jakarta.inject.Provider.class) {
                        return Optional.empty();
                    }
                    return (Optional<T>) Optional.ofNullable(getValue(componentRef))
                            .map(provider -> (jakarta.inject.Provider<Object>) () -> provider.get(this));
                }

                return Optional.ofNullable(getValue(componentRef))
                        .map(provider -> (T) provider.get(this));

            }

        };
    }

    private <T> Provider<?> getValue(ComponentRef<T> componentRef) {
        return components.get(componentRef.component());
    }

    private void checkDependencies(Component component, Stack<Component> visiting) {
        for (ComponentRef dependency : components.get(component).getDependencies()) {
            Component key = dependency.component();
            if (!components.containsKey(key)) {
                throw new DependencyNotFoundException(component, dependency.component());
            }

            if (!dependency.isContainer()) {
                if (visiting.contains(dependency.component())) {
                    throw new CyclicDependencyException(visiting);
                }
                visiting.push(dependency.component());
                checkDependencies(key, visiting);
                visiting.pop();

            }
        }
    }


}
