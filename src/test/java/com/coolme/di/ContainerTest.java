package com.coolme.di;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.inject.Qualifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.*;

class ContainerTest {


    ContextConfig config;

    @BeforeEach
    public void setup() {
        config = new ContextConfig();
    }

    @Nested
    public class DependencyInject {

        static class SkyWalkerDependency implements Dependency {

            @Inject
            public SkyWalkerDependency(@Named("ChooseOne") Dependency dependency) {

            }
        }

        static class NotCyclicDependency implements Dependency {
            @Inject
            public NotCyclicDependency(@SkyWalker Dependency dependency) {

            }
        }

        // TODO
        @Test
        public void should_not_throw_exception_if_not_same_qualifiers_given() {
            Dependency dependency = new Dependency() {
            };

            config.bind(Dependency.class, dependency,new NamedLiteral("ChooseOne"));
            config.bind(Dependency.class, SkyWalkerDependency.class, new SkyWalkerLiteral());
            config.bind(Dependency.class, NotCyclicDependency.class);

            assertDoesNotThrow(() -> config.getContext());
        }

        public record NamedLiteral(String value) implements Named {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Named.class;
            }

            @Override
            public boolean equals(Object o) {
                if (o instanceof Named named) {
                    return Objects.equals(value, named.value());
                }
                return false;
            }

            @Override
            public int hashCode() {
                return "value".hashCode() * 127 ^ value().hashCode();
            }
        }

        //  instance injection
        @Test
        public void should_bind_to_type_with_a_specified_instance() {
            InjectionTest.Component component = new InjectionTest.Component() {
            };
            config.bind(InjectionTest.Component.class, component);

            assertSame(((Optional) config.getContext().get(ComponentRef.of(InjectionTest.Component.class, null))).get(), component);
        }

        // inject with instance qualified
        @Test
        public void should_bind_type_with_instance_qualified() {
            InjectionTest.Component component = new InjectionTest.Component() {
            };
            config.bind(InjectionTest.Component.class, component, new NamedLiteral("ChosenOne"));

            assertSame(config.getContext().get(ComponentRef.of(InjectionTest.Component.class, new NamedLiteral("ChosenOne"))).get(), component);
        }

        //  bind type with class qualified
        @Test
        public void should_bind_type_with_class_qualified() {
            Dependency dependency = new Dependency() {
            };
            config.bind(Dependency.class, dependency);
            config.bind(ComponentWithInjectConstructor.class, ComponentWithInjectConstructor.class, new NamedLiteral("ChosenOne"));

            ComponentWithInjectConstructor component = config.getContext().get(ComponentRef.of(ComponentWithInjectConstructor.class, new NamedLiteral("ChosenOne"))).get();
            assertSame(component.getDependency(), dependency);
        }

        // inject with instance muliti qualified
        @Test
        public void should_bind_type_with_instance_multi_qualified() {
            InjectionTest.Component component = new InjectionTest.Component() {
            };
            config.bind(InjectionTest.Component.class, component, new NamedLiteral("ChosenOne"), new NamedLiteral("ChosenTwo"));

            assertSame(config.getContext().get(ComponentRef.of(InjectionTest.Component.class, new NamedLiteral("ChosenOne"))).get(), component);
            assertSame(config.getContext().get(ComponentRef.of(InjectionTest.Component.class, new NamedLiteral("ChosenTwo"))).get(), component);
        }

        //  bind type with class qualified
        @Test
        public void should_bind_type_with_class__multi_qualified() {
            Dependency dependency = new Dependency() {
            };
            config.bind(Dependency.class, dependency);
            config.bind(ComponentWithInjectConstructor.class, ComponentWithInjectConstructor.class,
                    new NamedLiteral("ChosenOne"),
                    new SkyWalkerLiteral());


            assertSame(config.getContext().get(ComponentRef.of(ComponentWithInjectConstructor.class,
                    new NamedLiteral("ChosenOne"))).get().getDependency(), dependency);
            assertSame(config.getContext().get(ComponentRef.of(ComponentWithInjectConstructor.class,
                    new SkyWalkerLiteral())).get().getDependency(), dependency);
        }

        @Test
        public void should_throw_exception_if_not_qualified_annotation_instance_added() {
            InjectionTest.Component component = new InjectionTest.Component() {
            };

            assertThrows(IllegalComponentException.class, () -> config.bind(InjectionTest.Component.class, component, new TestLiteral()));
        }

        @Test
        public void should_throw_exception_if_not_qualified_annotation_class_added() {
            assertThrows(IllegalComponentException.class, () -> config.bind(InjectionTest.Component.class, InjectionTest.Component.class, new TestLiteral()));
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
            config.bind(InjectionTest.Component.class, ComponentWithInjectConstructor.class);
            config.bind(Dependency.class, DependencyWithComponentInjected.class);

            CyclicDependencyException exception = assertThrows(CyclicDependencyException.class, () -> config.getContext());

            List<Class<?>> components = exception.getComponents();

            assertEquals(2, components.size());
            assertTrue(components.contains(InjectionTest.Component.class));
            assertTrue(components.contains(Dependency.class));

        }

        // A->B->C->A
        @Test
        public void should_throw_exception_of_transitive_cyclic_dependencies_happens() {
            config.bind(InjectionTest.Component.class, ComponentWithInjectConstructor.class);
            config.bind(Dependency.class, DependencyDependentOnAnotherDependency.class);
            config.bind(AnotherDependency.class, AnotherDependencyDependentOnComponent.class);

            CyclicDependencyException exception = assertThrows(CyclicDependencyException.class, () -> config.getContext());

            List<Class<?>> components = exception.getComponents();
            assertEquals(3, components.size());
            assertTrue(components.contains(InjectionTest.Component.class));
            assertTrue(components.contains(Dependency.class));
            assertTrue(components.contains(AnotherDependency.class));

        }

        // A->B->C
        @Test
        public void should_throw_exception_if_transitive_dependencies_not_found() {
            config.bind(Dependency.class, DependencyWithInjectConstructor.class);

            DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext());

            assertEquals(String.class, exception.getDependency().type());
            assertEquals(Dependency.class, exception.getComponent().type());
        }


        // could get Provider<T> from context
        @Test
        public void should_retrieve_bind_as_provider() {
            InjectionTest.Component instance = new InjectionTest.Component() {
            };
            config.bind(InjectionTest.Component.class, instance);
            Context context = config.getContext();

            Provider<InjectionTest.Component> provider = context.get(new ComponentRef<Provider<InjectionTest.Component>>() {
            }).get();
            assertSame(provider.get(), instance);

        }

        @Test
        public void should_retrieve_bind_as_unsupported_container() {
            InjectionTest.Component instance = new InjectionTest.Component() {
            };
            config.bind(InjectionTest.Component.class, instance);
            Context context = config.getContext();

            Optional<List<InjectionTest.Component>> components = context.get(new ComponentRef<>() {
            });
            assertFalse(components.isPresent());

        }

        static class CyclicDependencyProviderConstructor implements InjectionTest.Component {

            @Inject
            public CyclicDependencyProviderConstructor(Provider<Dependency> dependency) {
            }
        }

        static class CyclicComponentProviderConstructor implements Dependency {

            @Inject
            public CyclicComponentProviderConstructor(Provider<InjectionTest.Component> dependency) {
            }
        }

        @Test
        public void should_not_throw_exception_if_cyclic_dependencies_vi_provider() {
            config.bind(InjectionTest.Component.class, CyclicDependencyProviderConstructor.class);
            config.bind(Dependency.class, CyclicComponentProviderConstructor.class);

            Context context = config.getContext();
            assertTrue(context.get(ComponentRef.of(InjectionTest.Component.class, null)).isPresent());
        }

    }


    @Nested
    public class WithQualifier {

        // TODO should_throw_exception_if_qualified_dependency_not_found
        @Test
        public void should_throw_exception_if_qualified_dependency_not_found() {
            config.bind(Dependency.class, new Dependency() {

            });
            config.bind(InjectConstructor.class, InjectConstructor.class, new DependencyInject.NamedLiteral("Owner"));

            DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext());

            assertEquals(new Component(Dependency.class, new SkyWalkerLiteral()), exception.getDependency());
            assertEquals(new Component(InjectConstructor.class, new DependencyInject.NamedLiteral("Owner")), exception.getComponent());

        }

        static class InjectConstructor {

            @Inject
            public InjectConstructor(@SkyWalker Dependency dependency) {

            }
        }
    }
}


class ComponentWithDefaultConstructor implements InjectionTest.Component {

    public ComponentWithDefaultConstructor() {
    }
}

abstract class AbstractComponent implements InjectionTest.Component {

    @Inject
    public AbstractComponent() {

    }
}

class ComponentWithMultiInjectConstructors implements InjectionTest.Component {
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

class ComponentWithNoDefaultConstructorNorInjectConstructor implements InjectionTest.Component {

    private String name;

    private Double decimal;

    public ComponentWithNoDefaultConstructorNorInjectConstructor(String name, Double decimal) {
        this.name = name;
        this.decimal = decimal;
    }
}


class ComponentWithInjectConstructor implements InjectionTest.Component {

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

    private InjectionTest.Component component;

    @Inject
    public DependencyWithComponentInjected(InjectionTest.Component component) {
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

    private InjectionTest.Component component;

    @Inject
    public AnotherDependencyDependentOnComponent(InjectionTest.Component component) {
        this.component = component;
    }
}


@Target({ANNOTATION_TYPE, PARAMETER})
@Retention(RUNTIME)
@Documented
@Qualifier
@interface SkyWalker {

}

record SkyWalkerLiteral() implements SkyWalker {

    @Override
    public Class<? extends Annotation> annotationType() {
        return SkyWalker.class;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SkyWalker;
    }
}

record TestLiteral() implements Test {

    @Override
    public Class<? extends Annotation> annotationType() {
        return TestLiteral.class;
    }
}