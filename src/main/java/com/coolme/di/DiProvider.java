package com.coolme.di;

import java.util.List;

interface DiProvider<Type> {
    Type get(Context context);

    default List<Class<?>> getDependencies() {
        return List.of();
    }

    default List<java.lang.reflect.Type> getDependencyTypes() {
        return List.of();
    }
}
