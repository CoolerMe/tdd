package com.coolme.di;

import java.util.HashSet;
import java.util.Set;

public class CyclicDependencyException extends RuntimeException {

    private final Set<Class<?>> components = new HashSet<>();

    public CyclicDependencyException(Class<?> component) {
        components.add(component);
    }

    public CyclicDependencyException(Class<?> componentClass, CyclicDependencyException e) {
        components.add(componentClass);
        components.addAll(e.components);
    }

    public Set<Class<?>> getComponents() {
        return components;
    }
}
