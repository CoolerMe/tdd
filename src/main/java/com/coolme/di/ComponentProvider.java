package com.coolme.di;

import java.util.List;

interface ComponentProvider<T> {
    T get(Context context);

    default List<Context.Ref> getDependenciesRef() {
        return List.of();
    }

}
