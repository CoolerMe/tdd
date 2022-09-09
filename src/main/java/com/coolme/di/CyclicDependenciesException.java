package com.coolme.di;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CyclicDependenciesException extends RuntimeException {
    private final List<Class<?>> components = new ArrayList<>();

    public CyclicDependenciesException(Class<?> component) {
        this.components.add(component);
    }

    public CyclicDependenciesException(Class<?> component, CyclicDependenciesException exception) {
        this.components.add(component);
        this.components.addAll(exception.components);

    }

    public Set<Class<?>> getComponents() {
        return new HashSet<>(components);
    }
}
