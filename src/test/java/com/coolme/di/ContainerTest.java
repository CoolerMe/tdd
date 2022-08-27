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

    @Nested
    public class ComponentConstruction {

        //  instance injection
        @Test
        public void should_bind_to_type_with_a_specified_instance() {
            Component component = new Component() {

            };
            config.bind(Component.class, component);

            assertSame(config.getContext().get(Component.class).get(), component);
        }

        // TODO sad interface
        // TODO sad abstract class
        @Nested
        public class ConstructionInjection {

            // no args constructor
            @Test
            public void should_bind_type_to_a_class_with_default_constructor() {
                config.bind(Component.class, ComponentWithDefaultConstructor.class);

                Component component = config.getContext().get(Component.class).get();

                assertNotNull(component);
                assertTrue(component instanceof ComponentWithDefaultConstructor);
            }

            // with dependencies
            @Test
            public void should_bind_type_to_a_class_with_inject_constructor() {
                config.bind(Component.class, ComponentWithInjectConstructor.class);
                Dependency dependency = new Dependency() {
                };
                config.bind(Dependency.class, dependency);

                Component component = config.getContext().get(Component.class).get();

                assertNotNull(component);
                assertTrue(component instanceof ComponentWithInjectConstructor);
                assertEquals(((ComponentWithInjectConstructor) component).getDependency(), dependency);

            }

            // A->B->C
            @Test
            public void should_bind_type_to_a_class_with_transitive_dependencies() {
                config.bind(Component.class, ComponentWithInjectConstructor.class);
                config.bind(Dependency.class, DependencyWithInjectConstructor.class);
                config.bind(String.class, "String dependency");

                Component component = config.getContext().get(Component.class).get();
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
                    config.bind(Component.class, ComponentWithMultiInjectConstructors.class);
                });
            }

            // no default constructor and inject constructor
            @Test
            public void should_throw_exception_if_nor_default_constructor_nor_inject_constructor() {
                assertThrows(IllegalComponentException.class, () -> {
                    config.bind(Component.class, ComponentWithNoDefaultConstructorNorInjectConstructor.class);
                });
            }

            // dependency not exists
            @Test
            public void should_throw_exception_if_dependency_not_exists() {

                config.bind(Component.class, ComponentWithInjectConstructor.class);

                DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext());
                assertEquals(Dependency.class, exception.getDependency());

            }

            @Test
            public void should_return_empty_if_component_not_defined() {
                Optional<Component> component = config.getContext().get(Component.class);

                assertTrue(component.isEmpty());
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

                exception.getComponents().forEach(System.out::println);
                Set<Class<?>> components = exception.getComponents();
                assertEquals(3, components.size());
                assertTrue(components.contains(Component.class));
                assertTrue(components.contains(Dependency.class));
                assertTrue(components.contains(AnotherDependency.class));

            }

            // A->B->C
            @Test
            public void should_throw_exception_if_transitive_dependencies_not_found() {
                config.bind(Component.class, ComponentWithInjectConstructor.class);
                config.bind(Dependency.class, DependencyWithInjectConstructor.class);

                DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext());

                assertEquals(String.class, exception.getDependency());
                assertEquals(Dependency.class, exception.getComponent());
            }
        }

        @Nested
        public class FieldInjection {


            @Test
            public void should_inject_dependency_via_filed() {
                Dependency dependency = new Dependency() {
                };

                config.bind(Dependency.class, dependency);
                config.bind(Component.class, ComponentWithInjectFiled.class);

                ComponentWithInjectFiled component = (ComponentWithInjectFiled) config.getContext().get(Component.class).get();
                assertSame(dependency, component.dependency);

            }

            // sad path
            @Test
            public void should_throw_exception_if_inject_field_not_found() {
                config.bind(ComponentWithInjectFiled.class, ComponentWithInjectFiled.class);

                assertThrows(DependencyNotFoundException.class, () -> config.getContext());
            }

            //
            @Test
            public void should_include_field_dependency_in_dependencies() {
                InjectionProvider<ComponentWithInjectFiled> provider = new InjectionProvider<>(ComponentWithInjectFiled.class);

                assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray());
            }

            @Test
            public void should_inject_field_with_sub_class() {
                Dependency dependency = new Dependency() {
                };

                config.bind(Dependency.class, dependency);
                config.bind(SubComponentWithInjectFiled.class, SubComponentWithInjectFiled.class);

                SubComponentWithInjectFiled component = config.getContext().get(SubComponentWithInjectFiled.class).get();
                assertSame(dependency, component.dependency);
            }
        }

        @Nested
        public class MethodInjection {

            @Test
            public void should_call_method_if_inject_annotation_added() {
                config.bind(ComponentWithInjectMethod.class, ComponentWithInjectMethod.class);

                ComponentWithInjectMethod component = config.getContext().get(ComponentWithInjectMethod.class).get();
                assertTrue(component.called);

            }

            @Test
            public void should_inject_dependency_via_inject_method() {
                config.bind(ComponentWithInjectMethodAndDependency.class, ComponentWithInjectMethodAndDependency.class);
                Dependency dependency = new Dependency() {
                };

                config.bind(Dependency.class, dependency);

                ComponentWithInjectMethodAndDependency component = config.getContext().get(ComponentWithInjectMethodAndDependency.class).get();

                assertSame(dependency, component.dependency);

            }


            //
            @Test
            public void should_include_method_dependency_in_dependencies() {
                InjectionProvider<ComponentWithInjectMethodAndDependency> provider = new InjectionProvider<>(ComponentWithInjectMethodAndDependency.class);

                assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray());
            }

        }
    }


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