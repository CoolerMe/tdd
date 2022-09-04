package com.coolme.di;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

public class ComponentRef<T> {
    private Type container;
    private Component component;

    ComponentRef(Type type, Annotation qualifier) {
        init(type, qualifier);
    }

    protected ComponentRef() {
        Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        init(type, null);
    }

    static <T> ComponentRef<T> of(Class<T> component, Annotation qualifier) {
        return new ComponentRef<>(component, qualifier);
    }

    static ComponentRef of(Type type) {
        return new ComponentRef<>(type, null);
    }

    static ComponentRef of(Type type, Annotation qualifier) {
        return new ComponentRef<>(type, qualifier);
    }


    private void init(Type type, Annotation qualifier) {
        if (type instanceof ParameterizedType container) {
            this.container = container.getRawType();
            this.component = new Component((Class<?>) container.getActualTypeArguments()[0], qualifier);
        } else {
            this.component = new Component((Class<?>) type, qualifier);
        }
    }

    public Component component() {
        return component;
    }

    public Type getContainer() {
        return container;
    }

    public Class<?> getComponentType() {
        return component.type();
    }

    public boolean isContainer() {
        return container != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComponentRef<?> that = (ComponentRef<?>) o;

        if (!Objects.equals(container, that.container)) return false;
        return Objects.equals(component, that.component);
    }

    @Override
    public int hashCode() {
        int result = container != null ? container.hashCode() : 0;
        result = 31 * result + (component != null ? component.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ComponentRef{" +
                "container=" + container +
                ", component=" + component +
                '}';
    }
}
