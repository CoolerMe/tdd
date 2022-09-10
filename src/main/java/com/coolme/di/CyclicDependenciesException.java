package com.coolme.di;

import java.util.*;

public class CyclicDependenciesException extends RuntimeException {
    private final List<Class<?>> components = new ArrayList<>();

    public CyclicDependenciesException(Stack<Class<?>> visiting) {
        components.addAll(visiting);
    }

    public Set<Class<?>> getComponents() {
        return new HashSet<>(components);
    }
}
