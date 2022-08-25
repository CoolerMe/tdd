package com.coolme.di;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.stream;

public class Context {

    private final Map<Class<?>, Provider<?>> providers = new HashMap<>();

    public <Type> void bind(Class<Type> type, Type instance) {
        providers.put(type, () -> instance);
    }

    public <Type, Implementation extends Type> void bind(Class<Type> type, Class<Implementation> implementation) {
        Constructor<Implementation> constructor = getConstructor(implementation);
        providers.put(type, new ConstructorInjectionProvider<>(constructor, type));
    }

    public <Type> Optional<Type> get(Class<Type> type) {
        return Optional.ofNullable(providers.get(type))
                .map(it -> (Type) it.get());
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


    class ConstructorInjectionProvider<Type> implements Provider<Type> {

        private final Constructor<Type> constructor;

        private final Class<?> componentClass;

        private boolean constructing = false;

        public ConstructorInjectionProvider(Constructor<Type> constructor, Class<?> componentClass) {
            this.constructor = constructor;
            this.componentClass = componentClass;
        }

        @Override
        public Type get() {
            if (constructing) {
                throw new CyclicDependencyException(componentClass);
            }

            try {
                constructing = true;
                Object[] objects = stream(constructor.getParameters())
                        .map(it -> Context.this.get(it.getType())
                                .orElseThrow(() -> new DependencyNotFoundException(it.getType(), componentClass)))
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
