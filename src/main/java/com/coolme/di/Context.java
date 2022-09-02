package com.coolme.di;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

public interface Context {

    <T> Optional<T> get(Ref<T> ref);

    class Ref<T> {
        private Type container;
        private Class<?> component;

        Ref(Type type) {
            init(type);
        }

        Ref(Class<?> component) {
            init(component);
        }

        static <T> Ref<T> of(Class<T> component) {
            return new Ref(component);
        }

        static Ref of(Type type) {
            return new Ref(type);
        }

        protected Ref() {
            Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            init(type);
        }

        private void init(Type type) {
            if (type instanceof ParameterizedType container) {
                this.container = container.getRawType();
                this.component = (Class<?>) container.getActualTypeArguments()[0];
            } else {
                this.component = (Class<?>) type;
            }
        }

        public Type getContainer() {
            return container;
        }

        public Class<?> getComponent() {
            return component;
        }

        public boolean isContainer() {
            return container != null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Ref ref = (Ref) o;

            if (!Objects.equals(container, ref.container)) return false;
            return Objects.equals(component, ref.component);
        }

        @Override
        public int hashCode() {
            int result = container != null ? container.hashCode() : 0;
            result = 31 * result + (component != null ? component.hashCode() : 0);
            return result;
        }
    }
}
