package com.coolme.di;

import java.util.List;

interface Provider<Type> {
    Type get(Context context);

    List<Class<?>> getDependencies();
}
