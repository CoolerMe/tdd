package com.coolme.di;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CyclicDependencyException extends RuntimeException {

    private final Set<Class<?>> components = new HashSet<>();

    public CyclicDependencyException(List<Class<?>> visiting) {
        components.addAll(visiting);
    }

    public Set<Class<?>> getComponents() {
        return components;
    }
}
