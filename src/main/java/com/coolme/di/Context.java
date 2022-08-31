package com.coolme.di;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

public interface Context {

    <Type> Optional<Type> get(Class<Type> type);

    Optional get(ParameterizedType type);

    default Optional getType(Type type) {
        if (type instanceof ParameterizedType) {
            return get((ParameterizedType) type);
        } else {
            return get((Class<?>) type);
        }
    }
}
