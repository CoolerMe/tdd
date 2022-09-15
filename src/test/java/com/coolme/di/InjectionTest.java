package com.coolme.di;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Nested
class InjectionTest {
    ContextConfig config;

    @BeforeEach
    public void setup() {
        config = new ContextConfig();
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
                    () -> new ComponentInjectionProvider<>(ComponentWithMultiInjectConstructors.class));
        }

        //  no inject constructor nor default constructor
        @Test
        public void should_throw_exception_if_nor_inject_nor_default_constructor_provided() {
            assertThrows(IllegalComponentException.class,
                    () -> new ComponentInjectionProvider<>(ComponentWithNoInjectNorDefaultConstructor.class));
        }

        @Test
        public void should_inject_dependency_via_inject_constructor() {
            ComponentInjectionProvider<ComponentWithDependency> provider = new ComponentInjectionProvider<>(ComponentWithDependency.class);

            assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray());

        }


    }


    @Nested
    public class FieldInjection {

        @Test
        public void should_inject_dependency_via_field() {
            Dependency dependency = new Dependency() {
            };
            config.bind(Dependency.class, dependency);
            config.bind(Component.class, ComponentWithFieldInjection.class);

            ComponentWithFieldInjection component = (ComponentWithFieldInjection) config.getContext().get(Component.class).get();

            assertSame(dependency, component.dependency);
        }

        @Test
        public void should_inject_dependency_via_super_class_inject_field() {
            Dependency dependency = new Dependency() {
            };
            config.bind(Dependency.class, dependency);
            config.bind(Component.class, SuperClassWithFieldInjection.class);

            SuperClassWithFieldInjection component = (SuperClassWithFieldInjection) config.getContext().get(Component.class).get();

            assertSame(dependency, component.dependency);
        }

        @Test
        public void should_include_field_dependency_in_dependencies() {
            ComponentInjectionProvider<ComponentWithFieldInjection> component = new ComponentInjectionProvider<>(ComponentWithFieldInjection.class);
            List<Class<?>> dependencies = component.getDependencies();

            assertArrayEquals(new Class<?>[]{Dependency.class}, dependencies.toArray(Class<?>[]::new));

        }

        // final field
        @Test
        public void should_throw_exception_if_inject_field_is_final() {
            assertThrows(IllegalComponentException.class, () -> new ComponentInjectionProvider<>(ComponentWithFinalFiled.class));
        }
    }

    @Nested
    public class MethodInjection {

        @Test
        public void should_class_inject_method_even_if_no_dependency_declared() {
            config.bind(Component.class, ComponentWithMethodInjection.class);

            ComponentWithMethodInjection component = (ComponentWithMethodInjection) config.getContext().get(Component.class).get();

            assertTrue(component.installed);
        }

        @Test
        public void should_inject_dependency_via_method_injection() {
            Dependency dependency = new Dependency() {
            };
            config.bind(Dependency.class, dependency);
            config.bind(Component.class, ComponentWithDependencyMethodInjection.class);

            ComponentWithDependencyMethodInjection component = (ComponentWithDependencyMethodInjection) config.getContext()
                    .get(Component.class).get();

            assertEquals(dependency, component.dependency);
        }

        @Test
        public void should_include_dependencies_from_inject_method() {
            ComponentInjectionProvider<ComponentWithDependencyMethodInjection> provider
                    = new ComponentInjectionProvider<>(ComponentWithDependencyMethodInjection.class);

            assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray());
        }

        @Test
        public void should_inject_dependencies_via_method_injection_from_super_class() {
            config.bind(SubClassWithMethodInjection.class, SubClassWithMethodInjection.class);

            SubClassWithMethodInjection injection = config.getContext().get(SubClassWithMethodInjection.class).get();

            assertEquals(injection.subCalled, 2);
            assertEquals(injection.superCalled, 1);

        }

        @Test
        public void should_call_method_only_once_if_override_super_inject_method() {
            config.bind(SubClassWithOverrideInjectMethod.class, SubClassWithOverrideInjectMethod.class);

            SubClassWithOverrideInjectMethod component = config.getContext().get(SubClassWithOverrideInjectMethod.class).get();

            assertEquals(1, component.superCalled);
        }

        @Test
        public void should_not_call_method_if_override_and_no_inject_annotated() {
            config.bind(SubClassWithOverrideAndNoInject.class, SubClassWithOverrideAndNoInject.class);

            SubClassWithOverrideAndNoInject component = config.getContext().get(SubClassWithOverrideAndNoInject.class).get();

            assertEquals(0, component.superCalled);
        }

        //  parameterized type
        @Test
        public void should_throw_exception_if_method_inject_with_parameterized_type() {
            assertThrows(IllegalComponentException.class, () -> new ComponentInjectionProvider<>(ComponentWithParameterizedType.class));
        }
    }
}
