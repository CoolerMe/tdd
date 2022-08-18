package com.coolme.tdd;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coolme.tdd.exception.IllegalOptionException;
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

    @Test
    public void should_parse_list_option_if_flag_present() {

        ListOption listOption = Args.parse(ListOption.class, "-d", "-100", "200", "-g", "this",
            "is", "my", "cat");

        assertArrayEquals(listOption.group(), new String[]{"this", "is", "my", "cat"});
        assertArrayEquals(listOption.decimals(), new Integer[]{-100, 200});
    }

    record IllegalOption(@Option("p") int port, @Option("d") String directory, boolean logging) {

    }

    record ListOption(@Option("g") String[] group, @Option("d") Integer[] decimals) {

    }

    record MultiOption(@Option("l") boolean logging,
                       @Option("d") String directory,
                       @Option("p") int port) {


    }


}
