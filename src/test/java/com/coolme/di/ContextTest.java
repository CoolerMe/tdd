package com.coolme.di;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContextTest {

    interface Component {

    }

    static class ComponentWithDefaultConstructor implements Component {
        public ComponentWithDefaultConstructor() {
        }
    }

    //  instance
    @Test
    public void should_bind_class_to_a_specified_instance() {
        Context context = new Context();
        Component instance = new Component() {
        };
        context.bind(Component.class, instance);
        Component component = context.get(Component.class);

        assertSame(component, instance);
    }
    //  abstract class
    // TODO final class

    @Nested
    public class ConstructionInjection {
        //  no args constructor
        @Test
        public void should_bind_class_with_default_constructor() {
            Context context = new Context();
            context.bind(Component.class, ComponentWithDefaultConstructor.class);

            Component component = context.get(Component.class);

            assertNotNull(component);
            assertTrue(component instanceof ComponentWithDefaultConstructor);

        }
        // TODO with dependencies
        // TODO A->B->C
    }


    @Nested
    public class FieldInjection {

    }

    @Nested
    public class MethodInjection {

    }
}