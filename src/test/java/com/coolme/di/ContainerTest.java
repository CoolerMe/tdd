package com.coolme.di;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

            assertSame(context.get(Component.class), component);
        }

        // TODO sad interface
        // TODO sad abstract class
        @Nested
        public class ConstructionInjection {

            // no args constructor
            @Test
            public void should_bind_type_to_a_class_with_default_constructor() {
                context.bind(Component.class, ComponentWithDefaultConstructor.class);

                Component component = context.get(Component.class);

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

                Component component = context.get(Component.class);

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

                Component component = context.get(Component.class);
                assertNotNull(component);

                Dependency dependency = ((ComponentWithInjectConstructor) component).getDependency();
                assertNotNull(dependency);

                String value = ((DependencyWithInjectConstructor) dependency).getDependency();
                assertEquals(value, "String dependency");
            }

            // Sad path
            // TODO multi inject constructors
            @Test
            public void should_throw_exception_if_multi_inject_constructors_provided() {
                assertThrows(MultiInjectConstructorsException.class, () -> {
                    context.bind(Component.class, ComponentWithMultiInjectConstructors.class);
                });
            }
            // TODO no default constructor and inject constructor
            // TODO dependency not exists
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