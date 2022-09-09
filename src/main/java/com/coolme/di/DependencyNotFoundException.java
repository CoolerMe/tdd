package com.coolme.di;

public class DependencyNotFoundException extends RuntimeException {

    private final Class<?> componentType;

    private final Class<?> parameterType;

    public DependencyNotFoundException(Class<?> componentType, Class<?> parameterType) {
        this.componentType = componentType;
        this.parameterType = parameterType;
    }

    public Class<?> getComponentType() {
        return componentType;
    }

    public Class<?> getParameterType() {
        return parameterType;
    }
}
