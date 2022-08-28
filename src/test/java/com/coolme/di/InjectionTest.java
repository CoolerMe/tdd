package com.coolme.di;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;

@Nested
public class InjectionTest {


    private final Dependency dependency = Mockito.mock(Dependency.class);

    private final Context context = Mockito.mock(Context.class);

    @BeforeEach
    public void setup() {
        Mockito.when(context.get(eq(Dependency.class)))
                .thenReturn(Optional.of(dependency));
    }

    @Nested
    public class ConstructionInjection {


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
            ComponentWithDefaultConstructor component = new InjectionProvider<>(ComponentWithDefaultConstructor.class).get(context);
            assertNotNull(component);
        }


        // with dependencies
        @Test
        public void should_bind_type_to_a_class_with_inject_constructor() {
            ComponentWithInjectConstructor component = new InjectionProvider<>(ComponentWithInjectConstructor.class).get(context);

            assertNotNull(component);
            assertEquals(component.getDependency(), dependency);

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

    }

    @Nested
    public class FieldInjection {


        @Test
        public void should_inject_dependency_via_filed() {
            ComponentWithInjectFiled component = new InjectionProvider<>(ComponentWithInjectFiled.class).get(context);

            assertSame(dependency, component.dependency);
        }

        //
        @Test
        public void should_include_field_dependency_in_dependencies() {
            InjectionProvider<ComponentWithInjectFiled> provider = new InjectionProvider<>(ComponentWithInjectFiled.class);

            assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray());
        }

        @Test
        public void should_inject_field_with_sub_class() {
            SubComponentWithInjectFiled component = new InjectionProvider<>(SubComponentWithInjectFiled.class).get(context);

            assertSame(dependency, component.dependency);
        }

        static class ComponentWithFinalFiled implements Component {
            @Inject
            final Dependency dependency = null;
        }


        @Test
        public void should_throw_exception_if_inject_filed_is_final() {
            assertThrows(IllegalComponentException.class,
                    () -> new InjectionProvider<>(FieldInjection.ComponentWithFinalFiled.class));
        }

        static class ComponentWithGenericFiled<T> implements Component {
            @Inject
            <T> void install() {

            }
        }

        @Test
        public void should_throw_exception_if_inject_method_is_generic() {
            assertThrows(IllegalComponentException.class,
                    () -> new InjectionProvider<>(FieldInjection.ComponentWithGenericFiled.class));
        }
    }

    @Nested
    public class MethodInjection {

        @Test
        public void should_call_method_if_inject_annotation_added() {
            ComponentWithInjectMethod component = new InjectionProvider<>(ComponentWithInjectMethod.class).get(context);

            assertTrue(component.called);

        }

        @Test
        public void should_inject_dependency_via_inject_method() {
            ComponentWithInjectMethodAndDependency component = new InjectionProvider<>(ComponentWithInjectMethodAndDependency.class).get(context);

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
            SubClassWithInjectMethod component = new InjectionProvider<>(SubClassWithInjectMethod.class).get(context);

            assertEquals(2, component.subCalled);
            assertEquals(1, component.superCalled);

        }

        @Test
        public void should_inject_once_if_sub_class_override_method_from_super_class() {
            SubClassWithOverrideInjectMethod component = new InjectionProvider<>(SubClassWithOverrideInjectMethod.class).get(context);

            assertEquals(1, component.superCalled);
        }

        static class SubClassOverrideSuperClassWithNoInjectMethod extends MethodInjection.SuperClassWithInjectMethod {

            void install() {
                super.install();
            }
        }

        @Test
        public void should_not_inject_if_sub_class_override_super_inject_method_without_annotation() {
            SubClassOverrideSuperClassWithNoInjectMethod component = new InjectionProvider<>(SubClassOverrideSuperClassWithNoInjectMethod.class).get(context);

            assertEquals(0, component.superCalled);
        }

    }
}
