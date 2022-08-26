package com.coolme.di;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CyclicDependencyException extends RuntimeException {

    private final Set<Class<?>> components = new HashSet<>();

    public CyclicDependencyException(Class<?> dependency, List<Class<?>> visiting) {
        components.addAll(visiting);
        components.add(dependency);
    }

    public Set<Class<?>> getComponents() {
        return components;
    }
}
