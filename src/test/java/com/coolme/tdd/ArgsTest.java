package com.coolme.tdd;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author yeye.zeng 2022/8/16
 */
public class ArgsTest {

    // multi
    // TODO -l -p 8080 -d /usr/log
    @Test
    public void should_parse_multi_options() {
        MultiOption option = Args.parseBoolean(MultiOption.class, "-l", "-p", "8080", "-d",
            "/usr/log");

        assertTrue(option.logging);
        assertEquals(option.port, 8080);
        assertEquals(option.directory, "/usr/log");
    }

    // -g this is a cat
    @Test
    @Disabled
    public void should_pass_example2() {
        ListOption option = Args.parseBoolean(ListOption.class, "this", "is", "a", "cat");

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
