package com.coolme.di;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ContextTest {
    Context context;

    @BeforeEach
    public void setup() {
        context = new Context();
    }

    //  instance
    @Test
    public void should_bind_class_to_a_specified_instance() {
        Component instance = new Component() {
        };
        context.bind(Component.class, instance);
        Component component = context.get(Component.class).get();

        assertSame(component, instance);
    }

    // TODO abstract class
    // TODO final class
    //  get component
    @Test
    public void should_get_null_if_component_not_bind() {
        Optional<Component> component = context.get(Component.class);

        assertTrue(component.isEmpty());
    }

    @Nested
    public class ConstructionInjection {
        //  no args constructor
        @Test
        public void should_bind_class_with_default_constructor() {
            context.bind(Component.class, ComponentWithDefaultConstructor.class);

            Component component = context.get(Component.class).get();

            assertNotNull(component);
            assertTrue(component instanceof ComponentWithDefaultConstructor);

        }

        //  with dependencies
        @Test
        public void should_bind_type_to_a_class_with_inject_constructor() {
            Dependency dependency = new Dependency() {
            };
            context.bind(Dependency.class, dependency);
            context.bind(Component.class, ComponentWithInjectConstructor.class);

            Component component = context.get(Component.class).get();

            assertSame(((ComponentWithInjectConstructor) component).getDependency(), dependency);
        }

        //  A->B->C
        @Test
        public void should_bind_type_to_a_class_with_transitive_dependencies() {
            String content = "Hello! TDD";
            context.bind(String.class, content);
            context.bind(Dependency.class, DependencyWithInjectConstructor.class);
            context.bind(Component.class, ComponentWithInjectConstructor.class);

            Component component = context.get(Component.class).get();
            assertNotNull(component);
            ComponentWithInjectConstructor componentWithInjectConstructor = (ComponentWithInjectConstructor) component;

            DependencyWithInjectConstructor dependency = (DependencyWithInjectConstructor) componentWithInjectConstructor.getDependency();
            assertNotNull(dependency);

            assertSame(dependency.getContent(), content);
        }

        //  multi constructors
        @Test
        public void should_throw_exception_if_multi_inject_constructors_provided() {
            assertThrows(IllegalComponentException.class,
                    () -> context.bind(Component.class, ComponentWithMultiInjectConstructors.class));
        }

        //  no inject constructor nor default constructor
        @Test
        public void should_throw_exception_if_nor_inject_nor_default_constructor_provided() {
            assertThrows(IllegalComponentException.class,
                    () -> context.bind(Component.class, ComponentWithNoInjectNorDefaultConstructor.class));
        }

        @Test
        public void should_throw_exception_if_no_component_found() {
            context.bind(Component.class, ComponentWithInjectConstructor.class);

            assertThrows(DependencyNotFoundException.class, () -> context.get(Component.class));
        }

        // TODO cyclic dependencies
        @Test
        public void should_exception_if_cyclic_dependencies_found() {
            context.bind(Component.class, ComponentWithInjectConstructor.class);
            context.bind(Dependency.class, DependencyWithComponent.class);

            assertThrows(CyclicDependenciesException.class, () -> context.get(Component.class));
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

class ComponentWithInjectConstructor implements Component {

    private final Dependency dependency;

    @Inject
    public ComponentWithInjectConstructor(Dependency dependency) {
        this.dependency = dependency;
    }

    public Dependency getDependency() {
        return dependency;
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