package com.coolme.di;

import java.util.List;

interface Provider<T> {
    T get(Context context);

    default List<ComponentRef> getDependencies() {
        return List.of();
    }

}
