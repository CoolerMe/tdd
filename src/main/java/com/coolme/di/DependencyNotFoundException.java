package com.coolme.di;

public class DependencyNotFoundException extends RuntimeException {

    private Component component;

    private Component dependency;

    public DependencyNotFoundException(Component component, Component dependency) {
        this.component = component;
        this.dependency = dependency;
    }

    public Component getComponent() {
        return component;
    }

    public Component getDependency() {
        return dependency;
    }

    @Override
    public String toString() {
        return "DependencyNotFoundException{" +
                "component=" + component +
                ", dependency=" + dependency +
                '}';
    }
}
