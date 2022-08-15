package com.coolme.tdd;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author yeye.zeng 2022/8/16
 */
public class ArgsTest {

    // single
    // -boolean -l
    @Test
    public void should_set_boolean_option_to_true_if_flag_present() {
        BooleanOption option = Args.parseBoolean(BooleanOption.class, "-l");

        assertTrue(option.logging());
    }

    @Test
    public void should_set_boolean_option_to_flag_if_flag_not_present() {
        BooleanOption option = Args.parseBoolean(BooleanOption.class);

        assertFalse(option.logging());
    }

    static record BooleanOption(@Option("l") boolean logging) {

    }

    // -int -p 8080
    @Test
    public void should_parse_int_option_to_int_if_flag_present() {
        IntOption option = Args.parseBoolean(IntOption.class, "-p", "8080");

        assertEquals(option.port(), 8080);
    }

    static record IntOption(@Option("p") int port) {

    }

    // -String -d /usr/log
    @Test
    public void should_parse_string_option_if_flag_present() {
        StringOption option = Args.parseBoolean(StringOption.class, "-d", "/usr/log");

        assertEquals(option.directory(), "/usr/log");
    }

    static record StringOption(@Option("d") String directory) {

    }

    // multi
    // TODO -l -p 8080 -d /usr/log
    @Test
    public void should_parse_multi_options() {
        MultiOption option = Args.parseBoolean(MultiOption.class, "-l", "-p", "8080", "-d", "/usr/log");

        assertTrue(option.logging);
        assertEquals(option.port, 8080);
        assertEquals(option.directory, "/usr/log");
    }
    // sad path
    // TODO -boolean -l | -l t
    // TODO -int  -p 8080 8081 | -p
    // TODO-String -d | -d /usr/log /usr/vars

    // default path
    // TODO -boolean -l : true
    // TODO -int -p: 8080
    // TODO -String -d : ""


    // -g this is a cat
    @Test
    @Disabled
    public void should_pass_example2() {
        ListOption option = Args.parseBoolean(ListOption.class, "this", "is", "a", "cat");

        assertArrayEquals(new String[]{"this", "is", "a", "cat"}, option.group());
    }

    static record MultiOption(@Option("l") boolean logging,
                              @Option("d") String directory,
                              @Option("p") int port) {


    }

    static record ListOption(@Option("g") String[] group) {


    }

}
