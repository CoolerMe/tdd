package com.coolme.tdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class BooleanOptionParserTest {

    // happy path
    @Test
    public void should_parse_boolean_option_if_flag_present() {
        Boolean value = SingleValuedOptionParser.bool().parse(List.of("-l"), option("l"));

        assertTrue(value);
    }


    // sad path -l t
    @Test
    public void should_throw_exception_if_extra_argument_present() {
        TooManyArgumentException exception = assertThrows(
            TooManyArgumentException.class,
            () -> SingleValuedOptionParser.bool()
                .parse(Arrays.asList("-l", "t"), option("l"))
        );

        assertEquals(exception.getOption(), "l");
    }

    // default value
    @Test
    public void should_set_default_value_as_false_if_flag_not_present() {
        boolean result = SingleValuedOptionParser.bool().parse(List.of(), option("l"));

        assertFalse(result);
    }


    static Option option(String value) {
        return new Option() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Option.class;
            }

            @Override
            public String value() {
                return value;
            }
        };
    }
}
