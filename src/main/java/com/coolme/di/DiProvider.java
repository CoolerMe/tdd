package com.coolme.di;

import java.lang.reflect.Type;
import java.util.List;

interface DiProvider<T> {
    T get(Context context);

    default List<Class<?>> getDependencies() {
        return List.of();
    }

    default List<Type> getDependencyTypes() {
        return List.of();
    }
}
