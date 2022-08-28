package com.coolme.di;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Nested
public class InjectionTest {


    ContextConfig config;

    @BeforeEach
    public void setup() {
        config = new ContextConfig();
    }


    @Nested
    public class ConstructionInjection {

        //  instance injection
        @Test
        public void should_bind_to_type_with_a_specified_instance() {
            Component component = new Component() {

            };
            config.bind(Component.class, component);

            assertSame(config.getContext().get(Component.class).get(), component);
        }

        // sad abstract class
        @Test
        public void should_throw_exception_if_bind_abstract_class() {
            assertThrows(IllegalComponentException.class, () -> new InjectionProvider<>(AbstractComponent.class));

        }

        // sad interface
        @Test
        public void should_throw_exception_if_bind_interface() {
            assertThrows(IllegalComponentException.class, () -> new InjectionProvider<>(Component.class));
        }

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
                new InjectionProvider<>(ComponentWithMultiInjectConstructors.class);
            });
        }

        // no default constructor and inject constructor
        @Test
        public void should_throw_exception_if_nor_default_constructor_nor_inject_constructor() {
            assertThrows(IllegalComponentException.class, () -> {
                new InjectionProvider<>(ComponentWithNoDefaultConstructorNorInjectConstructor.class);
            });
        }


        @Test
        public void should_return_empty_if_component_not_defined() {
            Optional<Component> component = config.getContext().get(Component.class);

            assertTrue(component.isEmpty());
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

        static class ComponentWithFinalFiled implements Component {
            @Inject
            final Dependency dependency = null;
        }


        @Test
        public void should_throw_exception_if_inject_filed_is_final() {
            assertThrows(IllegalComponentException.class, () -> new InjectionProvider<>(FieldInjection.ComponentWithFinalFiled.class));
        }

        static class ComponentWithGenericFiled<T> implements Component {
            @Inject
            <T> void install() {

            }
        }

        @Test
        public void should_throw_exception_if_inject_method_is_generic() {
            assertThrows(IllegalComponentException.class, () -> new InjectionProvider<>(FieldInjection.ComponentWithGenericFiled.class));
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


        static class SuperClassWithInjectMethod {
            int superCalled = 0;

            @Inject
            void install() {
                superCalled++;
            }
        }

        static class SubClassWithInjectMethod extends MethodInjection.SuperClassWithInjectMethod {

            int subCalled = 0;

            @Inject
            void injectSub() {
                subCalled = superCalled + 1;
            }
        }

        static class SubClassWithOverrideInjectMethod extends MethodInjection.SuperClassWithInjectMethod {
            @Inject
            void install() {
                super.install();
            }
        }

        //  override inject method from supper class
        @Test
        public void should_inject_dependencies_via_super_class_method() {
            config.bind(MethodInjection.SubClassWithInjectMethod.class, MethodInjection.SubClassWithInjectMethod.class);

            MethodInjection.SubClassWithInjectMethod component = config.getContext().get(MethodInjection.SubClassWithInjectMethod.class).get();

            assertEquals(2, component.subCalled);
            assertEquals(1, component.superCalled);

        }

        @Test
        public void should_inject_once_if_sub_class_override_method_from_super_class() {
            config.bind(MethodInjection.SubClassWithOverrideInjectMethod.class, MethodInjection.SubClassWithOverrideInjectMethod.class);
            MethodInjection.SubClassWithOverrideInjectMethod component = config.getContext().get(MethodInjection.SubClassWithOverrideInjectMethod.class).get();

            assertEquals(1, component.superCalled);
        }

        static class SubClassOverrideSuperClassWithNoInjectMethod extends MethodInjection.SuperClassWithInjectMethod {

            void install() {
                super.install();
            }
        }

        @Test
        public void should_not_inject_if_sub_class_override_super_inject_method_without_annotation() {
            config.bind(MethodInjection.SubClassOverrideSuperClassWithNoInjectMethod.class, MethodInjection.SubClassOverrideSuperClassWithNoInjectMethod.class);
            MethodInjection.SubClassOverrideSuperClassWithNoInjectMethod component = config.getContext().get(MethodInjection.SubClassOverrideSuperClassWithNoInjectMethod.class).get();

            assertEquals(0, component.superCalled);
        }

    }
}
