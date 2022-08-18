package com.coolme.tdd;

import static com.coolme.tdd.BooleanOptionParserTest.option;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SingleValuedOptionParserTest {

    // happy path
    // -int -p 8080 -String -d /usr/log
    @Test
    public void should_parse_int_option_to_int_if_flag_present() {

        Object defaultValue = new Object();

        Object parsed = new Object();
        Function<String, Object> parser = (it) -> parsed;

        Object value = SingleValuedOptionParser.unary(defaultValue, parser)
            .parse(
            List.of("-p", "8080"),
            option("p"));

        assertSame(value, parsed);
    }

    //
    @Test
    public void should_parse_string_option_if_flag_present() {
        String value = SingleValuedOptionParser.unary("", String::valueOf).parse(
            List.of("-d", "/usr/log"),
            option("d"));

        assertEquals(value, "/usr/log");
    }


    // sad path -p 8080 8081
    @Test
    public void should_not_accept_extra_arguments() {
        TooManyArgumentException exception = assertThrows(TooManyArgumentException.class,
            () -> {
                SingleValuedOptionParser.unary(0, Integer::parseInt).parse(
                    List.of("-p", "8080", "8081"), option("p"));
            });

        assertEquals(exception.getOption(), "p");
    }

    // sad path -p -l| -p
    @ParameterizedTest
    @ValueSource(strings = {"-p -l", "-p"})
    public void should_not_accept_insufficient_argument(String argument) {
        InsufficientArgumentException exception = assertThrows(InsufficientArgumentException.class,
            () -> {
                SingleValuedOptionParser.unary(0, Integer::parseInt).parse(
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

        Object parsed = SingleValuedOptionParser.unary(defaultValue, parser)
            .parse(
            List.of(),
            option("p"));

        assertEquals(parsed, defaultValue);
    }


}
