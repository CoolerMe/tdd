package com.coolme.di;

import jakarta.inject.Inject;

// happy path
class ComponentWithInjectFiled implements Component {

    @Inject
    Dependency dependency;
}

class SubComponentWithInjectFiled extends ComponentWithInjectFiled {

}

class ComponentWithInjectMethod implements Component {

    boolean called = false;

    @Inject
    public void add() {
        called = true;
    }
}

class ComponentWithInjectMethodAndDependency implements Component {

    Dependency dependency;

    @Inject
    public void add(Dependency dependency) {
        this.dependency = dependency;
    }
}