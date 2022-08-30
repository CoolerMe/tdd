package com.coolme.di;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ContainerTest {


    ContextConfig config;

    @BeforeEach
    public void setup() {
        config = new ContextConfig();
    }

    @Nested
    public class DependencyInject {


        //  instance injection
        @Test
        public void should_bind_to_type_with_a_specified_instance() {
            Component component = new Component() {
            };
            config.bind(Component.class, component);

            assertSame(config.getContext().get(Component.class).get(), component);
        }


        // sad path
        @Test
        public void should_throw_exception_if_inject_field_not_found() {
            config.bind(ComponentWithInjectFiled.class, ComponentWithInjectFiled.class);

            assertThrows(DependencyNotFoundException.class, () -> config.getContext());
        }

        // A->B->A
        @Test
        public void should_throw_exception_if_cyclic_dependency_exists() {
            config.bind(Component.class, ComponentWithInjectConstructor.class);
            config.bind(Dependency.class, DependencyWithComponentInjected.class);

            CyclicDependencyException exception = assertThrows(CyclicDependencyException.class, () -> config.getContext());

            Set<Class<?>> components = exception.getComponents();

            assertEquals(2, components.size());
            assertTrue(components.contains(Component.class));
            assertTrue(components.contains(Dependency.class));

        }

        // A->B->C->A
        @Test
        public void should_throw_exception_of_transitive_cyclic_dependencies_happens() {
            config.bind(Component.class, ComponentWithInjectConstructor.class);
            config.bind(Dependency.class, DependencyDependentOnAnotherDependency.class);
            config.bind(AnotherDependency.class, AnotherDependencyDependentOnComponent.class);

            CyclicDependencyException exception = assertThrows(CyclicDependencyException.class, () -> config.getContext());

            Set<Class<?>> components = exception.getComponents();
            assertEquals(3, components.size());
            assertTrue(components.contains(Component.class));
            assertTrue(components.contains(Dependency.class));
            assertTrue(components.contains(AnotherDependency.class));

        }

        // A->B->C
        @Test
        public void should_throw_exception_if_transitive_dependencies_not_found() {
            config.bind(Dependency.class, DependencyWithInjectConstructor.class);

            DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext());

            assertEquals(String.class, exception.getDependency());
            assertEquals(Dependency.class, exception.getComponent());
        }


        // could get Provider<T> from context
        @Test
        public void should_retrieve_bind_as_provider() {
            Component instance = new Component() {
            };
            config.bind(Component.class, instance);
            Context context = config.getContext();

            ParameterizedType type = new TypeLiteral<Provider<Component>>() {
            }.getType();

            Provider<Component> provider = (Provider<Component>) context.get(type).get();
            assertSame(provider.get(), instance);

        }

        @Test
        public void should_retrieve_bind_as_unsupported_container() {
            Component instance = new Component() {
            };
            config.bind(Component.class, instance);
            Context context = config.getContext();

            ParameterizedType type = new TypeLiteral<List<Component>>() {
            }.getType();
            assertFalse(context.get(type).isPresent());

        }

        static abstract class TypeLiteral<T> {

            public ParameterizedType getType() {
                return (ParameterizedType) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            }
        }

    }


}


class ComponentWithDefaultConstructor implements Component {

    public ComponentWithDefaultConstructor() {
    }
}

abstract class AbstractComponent implements Component {

    @Inject
    public AbstractComponent() {

    }
}

class ComponentWithMultiInjectConstructors implements Component {
    private String content;
    private Double decimal;

    @Inject
    public ComponentWithMultiInjectConstructors(String content, Double decimal) {
        this.content = content;
        this.decimal = decimal;
    }

    @Inject
    public ComponentWithMultiInjectConstructors(String content) {
        this.content = content;
    }


}

class ComponentWithNoDefaultConstructorNorInjectConstructor implements Component {

    private String name;

    private Double decimal;

    public ComponentWithNoDefaultConstructorNorInjectConstructor(String name, Double decimal) {
        this.name = name;
        this.decimal = decimal;
    }
}


class ComponentWithInjectConstructor implements Component {

    private Dependency dependency;

    @Inject
    public ComponentWithInjectConstructor(Dependency dependency) {
        this.dependency = dependency;
    }

    public Dependency getDependency() {
        return dependency;
    }
}

class DependencyWithComponentInjected implements Dependency {

    private Component component;

    @Inject
    public DependencyWithComponentInjected(Component component) {
        this.component = component;
    }
}

class DependencyWithInjectConstructor implements Dependency {
    private String dependency;

    @Inject
    public DependencyWithInjectConstructor(String dependency) {
        this.dependency = dependency;
    }

    public void setDependency(String dependency) {
        this.dependency = dependency;
    }

    public String getDependency() {
        return dependency;
    }
}

interface AnotherDependency {

}

class DependencyDependentOnAnotherDependency implements Dependency {

    private AnotherDependency dependency;

    @Inject
    public DependencyDependentOnAnotherDependency(AnotherDependency dependency) {
        this.dependency = dependency;
    }
}

class AnotherDependencyDependentOnComponent implements AnotherDependency {

    private Component component;

    @Inject
    public AnotherDependencyDependentOnComponent(Component component) {
        this.component = component;
    }
}