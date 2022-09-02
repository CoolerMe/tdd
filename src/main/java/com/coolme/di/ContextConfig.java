package com.coolme.di;

import jakarta.inject.Provider;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

public class ContextConfig {

    private final Map<Class<?>, ComponentProvider<?>> providers = new HashMap<>();
    private final Map<Component, ComponentProvider<?>> components = new HashMap<>();

    public <T> void bind(Class<T> type, T instance) {
        providers.put(type, context -> instance);
    }

    public <T> void bind(Class<T> type, T instance, Annotation... qualifiers) {
        for (Annotation qualifier : qualifiers) {
            components.put(new Component(type, qualifier), context -> instance);
        }
    }


    public <T, I extends T> void bind(Class<T> type, Class<I> implementation) {
        providers.put(type, new InjectionProvider<>(implementation));
    }

    public <T, I extends T> void bind(Class<T> type, Class<I> implementation, Annotation... qualifiers) {
        for (Annotation qualifier : qualifiers) {
            components.put(new Component(type, qualifier), new InjectionProvider<>(implementation));
        }
    }

    private record Component(Class<?> componentClas, Annotation qualifier) {

    }

    public Context getContext() {
        providers.keySet().forEach(component -> checkDependencies(component, new Stack<>()));

        return new Context() {

            @Override
            public Optional get(Ref ref) {
                if (ref.getQualifer() != null) {
                    return Optional.ofNullable(components.get(new Component(ref.getComponent(), ref.getQualifer())))
                            .map(it -> it.get(this));
                }
                if (ref.isContainer()) {
                    if (ref.getContainer() != Provider.class) {
                        return Optional.empty();
                    }
                    return Optional.ofNullable(providers.get(ref.getComponent())).map(provider -> (Provider<Object>) (() -> provider.get(this)));
                } else {
                    return Optional.ofNullable(providers.get(ref.getComponent())).map(it -> it.get(this));
                }
            }

        };
    }


    private void checkDependencies(Class<?> component, Stack<Class<?>> visiting) {
        for (Context.Ref dependency : providers.get(component).getDependenciesRef()) {
            if (!providers.containsKey(dependency.getComponent())) {
                throw new DependencyNotFoundException(component, dependency.getComponent());
            }

            if (!dependency.isContainer()) {
                if (visiting.contains(dependency.getComponent())) {
                    throw new CyclicDependencyException(visiting);
                }
                visiting.push(dependency.getComponent());
                checkDependencies(dependency.getComponent(), visiting);
                visiting.pop();

            }
        }
    }


}
