package com.coolme.di;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CyclicDependencyException extends RuntimeException {

    private final Set<Component> components = new HashSet<>();

    public CyclicDependencyException(List<Component> visiting) {
        components.addAll(visiting);
    }

    public List<Class<?>> getComponents() {
        return components.stream()
                .map((Function<Component, Class<?>>) Component::type).collect(Collectors.toList());
    }
}
