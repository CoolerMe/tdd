package com.coolme.di;

import com.coolme.di.ContainerTest.DependencyInject.NamedLiteral;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Nested
public class InjectionTest {


    private final Dependency dependency = mock(Dependency.class);
    private Provider<Dependency> dependencyProvider = mock(Provider.class);

    private final Context context = mock(Context.class);
    private ParameterizedType parameterizedType;

    @BeforeEach
    public void setup() throws NoSuchFieldException {

        parameterizedType = (ParameterizedType) InjectionTest.class.getDeclaredField("dependencyProvider").getGenericType();
        when((Optional) context.get(eq(ComponentRef.of(Dependency.class, null))))
                .thenReturn(Optional.of(dependency));
        when(context.get(eq(ComponentRef.of(parameterizedType))))
                .thenReturn(Optional.of(dependencyProvider));
    }

    @Nested
    public class ConstructionInjection {

        @BeforeEach
        public void setup() {
            Mockito.reset(context);
            when(context.get(eq(ComponentRef.of(Dependency.class, new NamedLiteral("ChosenOne")))))
                    .thenReturn(Optional.of(dependency));
        }

        static class ProviderInjectConstructor {

            private Provider<Dependency> dependency;

            @Inject
            public ProviderInjectConstructor(Provider<Dependency> dependency) {
                this.dependency = dependency;
            }
        }


        static class InjectConstructor {
            Dependency dependency;

            @Inject
            public InjectConstructor(@Named("ChosenOne") Dependency dependency) {
                this.dependency = dependency;
            }
        }

        // inject with qualifier
        @Test
        public void should_inject_dependency_with_qualified_annotation_present() {
            InjectionProvider<InjectConstructor> provider = new InjectionProvider<>(InjectConstructor.class);
            InjectConstructor component = provider.get(context);

            assertSame(dependency, component.dependency);
        }


        // inject construct
        @Test
        @Disabled
        public void should_inject_dependency_via_inject_constructor() {
            InjectionProvider<ProviderInjectConstructor> provider = new InjectionProvider<>(ProviderInjectConstructor.class);
            ProviderInjectConstructor providerInjectConstructor = provider.get(context);

            assertSame(providerInjectConstructor.dependency, dependencyProvider);
        }

        @Test
        public void should_get_dependency_type_from_inject_constructor() {
            InjectionProvider<ProviderInjectConstructor> provider = new InjectionProvider<>(ProviderInjectConstructor.class);
            assertArrayEquals(provider.getDependencies().toArray(ComponentRef[]::new), new ComponentRef[]{ComponentRef.of(parameterizedType)});
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
            ComponentWithDefaultConstructor component = new InjectionProvider<>(ComponentWithDefaultConstructor.class).get(context);
            assertNotNull(component);
        }


        // with dependencies
        @Test
        @Disabled
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

        @BeforeEach
        public void setup() {
            Mockito.reset(context);
            when(context.get(eq(ComponentRef.of(Dependency.class, new NamedLiteral("ChosenOne")))))
                    .thenReturn(Optional.of(dependency));
        }

        // inject filed
        static class ProviderInjectField {

            @Inject
            Provider<Dependency> dependency;

        }


        static class InjectMethod {

            @Inject
            @Named("ChosenOne")
            Dependency dependency;

        }

        // inject with qualifier
        @Test
        public void should_inject_dependency_with_qualified_annotation_present() {
            InjectionProvider<InjectMethod> provider = new InjectionProvider<>(InjectMethod.class);
            InjectMethod component = provider.get(context);

            assertSame(dependency, component.dependency);
        }

        @Test
        public void should_inject_dependency_via_inject_field() {
            ProviderInjectField providerInjectField = new InjectionProvider<>(ProviderInjectField.class).get(context);

            assertSame(providerInjectField.dependency, dependencyProvider);
        }

        @Test
        public void should_inject_dependency_via_filed() {
            ComponentWithInjectFiled component = new InjectionProvider<>(ComponentWithInjectFiled.class).get(context);

            assertSame(dependency, component.dependency);
        }

        //
        @Test
        public void should_include_field_dependency_in_dependencies() {
            InjectionProvider<ComponentWithInjectFiled> provider = new InjectionProvider<>(ComponentWithInjectFiled.class);

            assertArrayEquals(new ComponentRef[]{ComponentRef.of(Dependency.class, null)}, provider.getDependencies().toArray());
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


        // TODO inject with qualifier
        static class QualifierFieldInjection {

            @Inject
            @Named("ChosenOne")
            private Dependency dependency;
        }

        @Test
        public void should_inject_dependency_witch_method_qualified_inject() {
            InjectionProvider<QualifierFieldInjection> provider = new InjectionProvider<>(QualifierFieldInjection.class);

            assertArrayEquals(new ComponentRef<?>[]{
                    new ComponentRef<>(Dependency.class, new NamedLiteral("ChosenOne"))
            }, provider.getDependencies().toArray());
        }


    }

    @Nested
    public class MethodInjection {

        static class ProviderInjectMethod {

            private Provider<Dependency> dependency;

            @Inject
            public void install(Provider<Dependency> dependency) {
                this.dependency = dependency;
            }
        }

        // inject method
        @Test
        public void should_inject_provider_dependency_via_inject_method() {
            ProviderInjectMethod providerInjectMethod = new InjectionProvider<>(ProviderInjectMethod.class).get(context);

            assertSame(providerInjectMethod.dependency, dependencyProvider);
        }

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

            assertArrayEquals(new ComponentRef[]{ComponentRef.of(Dependency.class, null)}, provider.getDependencies().toArray());
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

        // TODO inject with qualifier
        static class QualifierMethodInjection {

            @Inject
            void install(@Named("ChosenOne") Dependency dependency) {

            }
        }

        @Test
        public void should_inject_dependency_witch_method_qualified_inject() {
            InjectionProvider<QualifierMethodInjection> provider = new InjectionProvider<>(QualifierMethodInjection.class);

            assertArrayEquals(new ComponentRef<?>[]{
                    new ComponentRef<>(Dependency.class, new NamedLiteral("ChosenOne"))
            }, provider.getDependencies().toArray());
        }

    }

    @Nested
    public class WithQualifier {
        static class InjectConstructor {

            @Inject
            public InjectConstructor(@Named("ChooseOne") Dependency dependency) {

            }
        }

        // TODO should_throw_exception_if_qualified_dependency_not_found
        @Test
        public void should_throw_exception_if_qualified_dependency_not_found() {
            InjectionProvider<InjectConstructor> provider = new InjectionProvider<>(InjectConstructor.class);

            assertArrayEquals(new ComponentRef<?>[]{
                    new ComponentRef<>(Dependency.class, new NamedLiteral("ChooseOne"))
            }, provider.getDependencies().toArray());
        }
    }

    static interface Component {

    }
}
