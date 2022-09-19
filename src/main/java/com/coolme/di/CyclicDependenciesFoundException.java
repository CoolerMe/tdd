package com.coolme.di;

import java.util.*;

public class CyclicDependenciesFoundException extends RuntimeException {

    private final List<Class<?>> components = new ArrayList<>();

    public CyclicDependenciesFoundException(Stack<Class<?>> visiting) {
        components.addAll(visiting);
    }

    public Set<Class<?>> getComponents() {
        return new HashSet<>(components);
    }
}
