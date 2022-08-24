package com.coolme.di;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ContainerTest {


    Context context;

    @BeforeEach
    public void setup() {
        context = new Context();
    }

    @Nested
    public class ComponentConstruction {

        //  instance injection
        @Test
        public void should_bind_to_type_with_a_specified_instance() {
            Component component = new Component() {

            };
            context.bind(Component.class, component);

            assertSame(context.get(Component.class).get(), component);
        }

        // TODO sad interface
        // TODO sad abstract class
        @Nested
        public class ConstructionInjection {

            // no args constructor
            @Test
            public void should_bind_type_to_a_class_with_default_constructor() {
                context.bind(Component.class, ComponentWithDefaultConstructor.class);

                Component component = context.get(Component.class).get();

                assertNotNull(component);
                assertTrue(component instanceof ComponentWithDefaultConstructor);
            }

            // with dependencies
            @Test
            public void should_bind_type_to_a_class_with_inject_constructor() {
                context.bind(Component.class, ComponentWithInjectConstructor.class);
                Dependency dependency = new Dependency() {
                };
                context.bind(Dependency.class, dependency);

                Component component = context.get(Component.class).get();

                assertNotNull(component);
                assertTrue(component instanceof ComponentWithInjectConstructor);
                assertEquals(((ComponentWithInjectConstructor) component).getDependency(), dependency);

            }

            // A->B->C
            @Test
            public void should_bind_type_to_a_class_with_transitive_dependencies() {
                context.bind(Component.class, ComponentWithInjectConstructor.class);
                context.bind(Dependency.class, DependencyWithInjectConstructor.class);
                context.bind(String.class, "String dependency");

                Component component = context.get(Component.class).get();
                assertNotNull(component);

                Dependency dependency = ((ComponentWithInjectConstructor) component).getDependency();
                assertNotNull(dependency);

                String value = ((DependencyWithInjectConstructor) dependency).getDependency();
                assertEquals(value, "String dependency");
            }

            // Sad path
            // multi inject constructors
            @Test
            public void should_throw_exception_if_multi_inject_constructors_provided() {
                assertThrows(MultiInjectConstructorsException.class, () -> {
                    context.bind(Component.class, ComponentWithMultiInjectConstructors.class);
                });
            }

            // no default constructor and inject constructor
            @Test
            public void should_throw_exception_if_nor_default_constructor_nor_inject_constructor() {
                assertThrows(IllegalComponentException.class, () -> {
                    context.bind(Component.class, ComponentWithNoDefaultConstructorNorInjectConstructor.class);
                });
            }

            // dependency not exists
            @Test
            public void should_throw_exception_if_dependency_not_exists() {

                context.bind(Component.class, ComponentWithInjectConstructor.class);

                DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> context.get(Component.class).get());
                assertEquals(Dependency.class, exception.getDependency());

            }

            @Test
            public void should_return_empty_if_component_not_defined() {
                Optional<Component> component = context.get(Component.class);

                assertTrue(component.isEmpty());
            }

            // A->B->A
            @Test
            public void should_throw_exception_if_cyclic_dependency_exists() {
                context.bind(Component.class, ComponentWithInjectConstructor.class);
                context.bind(Dependency.class, DependencyWithComponentInjected.class);

                assertThrows(CyclicDependencyException.class, () -> context.get(Dependency.class).get());

            }

            // A->B->C->A
            @Test
            public void should_throw_exception_of_transitive_cyclic_dependencies_happens() {
                context.bind(Component.class, ComponentWithInjectConstructor.class);
                context.bind(Dependency.class, DependencyDependentOnAnotherDependency.class);
                context.bind(AnotherDependency.class, AnotherDependencyDependentOnComponent.class);

                assertThrows(CyclicDependencyException.class, () -> context.get(AnotherDependency.class).get());

            }

            // A->B->C
            @Test
            public void should_throw_exception_if_transitive_dependencies_not_found() {
                context.bind(Component.class, ComponentWithInjectConstructor.class);
                context.bind(Dependency.class, DependencyWithInjectConstructor.class);

                DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> context.get(Component.class).get());

                assertEquals(String.class, exception.getDependency());
                assertEquals(Dependency.class, exception.getComponent());
            }
        }

        @Nested
        public class FieldInjection {

        }

        @Nested
        public class MethodInjection {

        }
    }


}

interface Component {

}

interface Dependency {

}

class ComponentWithDefaultConstructor implements Component {

    public ComponentWithDefaultConstructor() {
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