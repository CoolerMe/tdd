package com.coolme.di;

import jakarta.inject.Provider;

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
        providers.keySet().forEach(component -> checkDependencies(component, new Stack<>()));

        return new Context() {

            @Override
            public Optional get(Type type) {
                return get(Ref.of(type));
            }

            private Optional<?> get(Ref ref) {
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
        for (Type dependency : providers.get(component).getDependencyTypes()) {
            Ref ref = Ref.of(dependency);

            if (!providers.containsKey(ref.getComponent())) {
                throw new DependencyNotFoundException(component, ref.getComponent());
            }

            if (!ref.isContainer()) {
                if (visiting.contains(ref.getComponent())) {
                    throw new CyclicDependencyException(visiting);
                }
                visiting.push(ref.getComponent());
                checkDependencies(ref.getComponent(), visiting);
                visiting.pop();

            }
        }
    }


}
