package com.coolme.di;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ContainerTest {
    ContextConfig config;

    @BeforeEach
    public void setup() {
        config = new ContextConfig();
    }

    //  instance
    @Test
    public void should_bind_class_to_a_specified_instance() {
        Component instance = new Component() {
        };
        config.bind(Component.class, instance);
        Component component = config.getContext().get(Component.class).get();

        assertSame(component, instance);
    }

    // TODO abstract class
    // TODO final class
    //  get component
    @Test
    public void should_get_null_if_component_not_bind() {
        Optional<Component> component = config.getContext().get(Component.class);

        assertTrue(component.isEmpty());
    }

    @Nested
    public class ConstructionInjection {
        //  no args constructor
        @Test
        public void should_bind_class_with_default_constructor() {
            config.bind(Component.class, ComponentWithDefaultConstructor.class);

            Component component = config.getContext().get(Component.class).get();

            assertNotNull(component);
            assertTrue(component instanceof ComponentWithDefaultConstructor);

        }

        //  with dependencies
        @Test
        public void should_bind_type_to_a_class_with_inject_constructor() {
            Dependency dependency = new Dependency() {
            };
            config.bind(Dependency.class, dependency);
            config.bind(Component.class, ComponentWithDependency.class);

            Component component = config.getContext().get(Component.class).get();

            assertSame(((ComponentWithDependency) component).getDependency(), dependency);
        }

        //  A->B->C
        @Test
        public void should_bind_type_to_a_class_with_transitive_dependencies() {
            String content = "Hello! TDD";
            config.bind(String.class, content);
            config.bind(Dependency.class, DependencyWithInjectConstructor.class);
            config.bind(Component.class, ComponentWithDependency.class);

            Component component = config.getContext().get(Component.class).get();
            assertNotNull(component);
            ComponentWithDependency componentWithDependency = (ComponentWithDependency) component;

            DependencyWithInjectConstructor dependency = (DependencyWithInjectConstructor) componentWithDependency.getDependency();
            assertNotNull(dependency);

            assertSame(dependency.getContent(), content);
        }

        //  multi constructors
        @Test
        public void should_throw_exception_if_multi_inject_constructors_provided() {
            assertThrows(IllegalComponentException.class,
                    () -> config.bind(Component.class, ComponentWithMultiInjectConstructors.class));
        }

        //  no inject constructor nor default constructor
        @Test
        public void should_throw_exception_if_nor_inject_nor_default_constructor_provided() {
            assertThrows(IllegalComponentException.class,
                    () -> config.bind(Component.class, ComponentWithNoInjectNorDefaultConstructor.class));
        }

        @Test
        public void should_throw_exception_if_no_component_found() {
            config.bind(Component.class, ComponentWithDependency.class);

            DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext());
            assertEquals(Component.class, exception.getComponentType());
            assertEquals(Dependency.class, exception.getParameterType());
        }

        @Test
        public void should_throw_exception_if_dependencies_not_found() {
            config.bind(Component.class, ComponentWithDependency.class);
            config.bind(Dependency.class, DependencyWithInjectConstructor.class);

            DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext());

            assertEquals(Dependency.class, exception.getComponentType());
            assertEquals(String.class, exception.getParameterType());
        }

        @Test
        public void should_throw_exception_if_transitive_dependencies_not_found() {
            config.bind(Component.class, ComponentWithDependency.class);
            config.bind(Dependency.class, DependencyWithAnotherDependency.class);
            config.bind(AnotherDependency.class, AnotherDependencyWithContent.class);

            assertThrows(DependencyNotFoundException.class, () -> config.getContext());
        }

        //  cyclic dependencies
        @Test
        public void should_exception_if_cyclic_dependencies_found() {
            config.bind(Component.class, ComponentWithDependency.class);
            config.bind(Dependency.class, DependencyWithComponent.class);

            CyclicDependenciesException exception = assertThrows(CyclicDependenciesException.class,
                    () -> config.getContext());

            Set<Class<?>> components = exception.getComponents();

            assertTrue(components.contains(Dependency.class));
            assertTrue(components.contains(Component.class));

            assertEquals(2, components.size());
        }

        // cyclic transitive dependencies
        @Test
        public void should_throw_exception_if_cyclic_transitive_dependencies_found() {
            config.bind(Component.class, ComponentWithDependency.class);
            config.bind(Dependency.class, DependencyWithAnotherDependency.class);
            config.bind(AnotherDependency.class, AnotherDependencyWithComponent.class);

            CyclicDependenciesException exception = assertThrows(CyclicDependenciesException.class,
                    () -> config.getContext());
            Set<Class<?>> components = exception.getComponents();
            assertEquals(3, components.size());
            assertTrue(components.contains(Component.class));
            assertTrue(components.contains(Dependency.class));
            assertTrue(components.contains(AnotherDependency.class));
        }
    }


    @Nested
    public class FieldInjection {

    }

    @Nested
    public class MethodInjection {

    }
}


interface Component {

}

interface Dependency {

}

interface AnotherDependency {

}

class AnotherDependencyWithContent implements AnotherDependency {
    private String content;

    @Inject
    public AnotherDependencyWithContent(String content) {
        this.content = content;
    }
}

class ComponentWithNoInjectNorDefaultConstructor implements Component {

    public ComponentWithNoInjectNorDefaultConstructor(String name) {
    }
}

class ComponentWithDefaultConstructor implements Component {
    public ComponentWithDefaultConstructor() {
    }
}

class ComponentWithMultiInjectConstructors implements Component {

    @Inject
    public ComponentWithMultiInjectConstructors(String name, Double decimal) {
    }

    @Inject

    public ComponentWithMultiInjectConstructors(String name) {
    }
}

class ComponentWithDependency implements Component {

    private final Dependency dependency;

    @Inject
    public ComponentWithDependency(Dependency dependency) {
        this.dependency = dependency;
    }

    public Dependency getDependency() {
        return dependency;
    }
}

class DependencyWithAnotherDependency implements Dependency {

    private final AnotherDependency dependency;

    @Inject
    public DependencyWithAnotherDependency(AnotherDependency dependency) {
        this.dependency = dependency;
    }
}

class AnotherDependencyWithComponent implements AnotherDependency {
    private final Component component;

    @Inject
    public AnotherDependencyWithComponent(Component component) {
        this.component = component;
    }
}

class DependencyWithComponent implements Dependency {
    private Component component;

    @Inject
    public DependencyWithComponent(Component component) {
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }
}

class DependencyWithInjectConstructor implements Dependency {

    private String content;

    @Inject
    public DependencyWithInjectConstructor(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}