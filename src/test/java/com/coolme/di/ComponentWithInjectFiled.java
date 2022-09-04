package com.coolme.di;

import jakarta.inject.Inject;

// happy path
class ComponentWithInjectFiled implements InjectionTest.Component {

    @Inject
    Dependency dependency;
}

class SubComponentWithInjectFiled extends ComponentWithInjectFiled {

}

class ComponentWithInjectMethod implements InjectionTest.Component {

    boolean called = false;

    @Inject
    public void add() {
        called = true;
    }
}

class ComponentWithInjectMethodAndDependency implements InjectionTest.Component {

    Dependency dependency;

    @Inject
    public void add(Dependency dependency) {
        this.dependency = dependency;
    }
}