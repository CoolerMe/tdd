package com.coolme.tdd;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coolme.tdd.exception.IllegalOptionException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author yeye.zeng 2022/8/16
 */
public class ArgsTest {

    // multi
    //  -l -p 8080 -d /usr/log
    @Test
    public void should_parse_multi_options() {
        MultiOption option = Args.parse(MultiOption.class, "-l", "-p", "8080", "-d",
            "/usr/log");

        assertTrue(option.logging);
        assertEquals(option.port, 8080);
        assertEquals(option.directory, "/usr/log");
    }

    @Test
    public void should_throw_illegal_option_exception_if_option_not_present() {
        IllegalOptionException exception = assertThrows(IllegalOptionException.class,
            () -> Args.parse(IllegalOption.class, "-p", "8080", "-l", "-d", "/usr/log"));

        assertEquals(exception.getParameter(), "logging");

    }

    record IllegalOption(@Option("-p") int port, @Option("-d") String directory, boolean logging) {

    }


    // -g this is a cat
    @Test
    @Disabled
    public void should_pass_example2() {
        ListOption option = Args.parse(ListOption.class, "this", "is", "a", "cat");

        assertArrayEquals(new String[]{"this", "is", "a", "cat"}, option.group());
    }


    record StringOption(@Option("d") String directory) {

    }

    record MultiOption(@Option("l") boolean logging,
                       @Option("d") String directory,
                       @Option("p") int port) {


    }

    record ListOption(@Option("g") String[] group) {


    }

}
