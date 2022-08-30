package com.coolme.di;

import java.util.List;

interface DiProvider<Type> {
    Type get(Context context);

    List<Class<?>> getDependencies();
}
