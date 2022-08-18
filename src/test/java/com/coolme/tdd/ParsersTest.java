package com.coolme.tdd;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ParsersTest {

    @Nested
    public class BooleanOptionParserTest {

        // happy path
        @Test
        public void should_parse_boolean_option_if_flag_present() {
            Boolean value = Parsers.bool().parse(List.of("-l"), option("l"));

            assertTrue(value);
        }


        // sad path -l t
        @Test
        public void should_throw_exception_if_extra_argument_present() {
            TooManyArgumentException exception = assertThrows(
                TooManyArgumentException.class,
                () -> Parsers.bool()
                    .parse(Arrays.asList("-l", "t"), option("l"))
            );

            assertEquals(exception.getOption(), "l");
        }

        // default value
        @Test
        public void should_set_default_value_as_false_if_flag_not_present() {
            boolean result = Parsers.bool().parse(List.of(), option("l"));

            assertFalse(result);
        }

    }


    @Nested
    public class UnaryOptionParserTest {

        // happy path
        // -int -p 8080 -String -d /usr/log
        @Test
        public void should_parse_int_option_to_int_if_flag_present() {

            Object defaultValue = new Object();

            Object parsed = new Object();
            Function<String, Object> parser = (it) -> parsed;

            Object value = Parsers.unary(defaultValue, parser)
                .parse(
                    List.of("-p", "8080"),
                    option("p"));

            assertSame(value, parsed);
        }

        //
        @Test
        public void should_parse_string_option_if_flag_present() {
            String value = Parsers.unary("", String::valueOf).parse(
                List.of("-d", "/usr/log"),
                option("d"));

            assertEquals(value, "/usr/log");
        }


        // sad path -p 8080 8081
        @Test
        public void should_not_accept_extra_arguments() {
            TooManyArgumentException exception = assertThrows(TooManyArgumentException.class,
                () -> {
                    Parsers.unary(0, Integer::parseInt).parse(
                        List.of("-p", "8080", "8081"), option("p"));
                });

            assertEquals(exception.getOption(), "p");
        }

        // sad path -p -l| -p
        @ParameterizedTest
        @ValueSource(strings = {"-p -l", "-p"})
        public void should_not_accept_insufficient_argument(String argument) {
            InsufficientArgumentException exception = assertThrows(
                InsufficientArgumentException.class,
                () -> {
                    Parsers.unary(0, Integer::parseInt).parse(
                        List.of(argument.split(" ")),
                        option("p"));
                });

            assertEquals(exception.getOption(), "p");
        }

        // default value
        @Test
        public void should_set_default_value_if_not_present() {
            Function<String, Object> parser = s -> null;
            Object defaultValue = new Object();

            Object parsed = Parsers.unary(defaultValue, parser)
                .parse(
                    List.of(),
                    option("p"));

            assertEquals(parsed, defaultValue);
        }

    }

    @Nested
    public class ListOptionParserTest {

        // happy path
        // -g this is my cat
        @Test
        public void should_parse_list_option_if_flag_present() {
            String[] values = Parsers.list(String[]::new, String::valueOf)
                .parse(List.of("-g", "this", "is", "my", "cat"), option("g"));

            assertArrayEquals(values, new String[]{"this", "is", "my", "cat"});
        }

        @Test
        public void should_set_default_value_to_empty_array() {
            String[] values = Parsers.list(String[]::new, String::valueOf)
                .parse(List.of(), option("g"));

            assertArrayEquals(values, new String[]{});
        }

        @Test
        public void should_throw_exception_if_arguments_ca_not_be_parsed() {
            Function<String, Object> function = it -> {
                throw new RuntimeException();
            };

            IllegalValueException exception = assertThrows(IllegalValueException.class,
                () -> Parsers.list(String[]::new, function)
                    .parse(List.of("-g", "this", "is", "my", "cat"), option("g")));

            assertEquals(exception.getOption(), "g");
            assertEquals(exception.getValue(), "this");
        }

        @Test
        public void shou_parse_list_option_when_negative_int_exists() {
            Integer[] values = Parsers.list(Integer[]::new, Integer::parseInt)
                .parse(List.of("-d", "-100", "200"), option("d"));

            assertArrayEquals(values, new Integer[]{-100, 200});
        }
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
