package com.coolme.tdd;

import java.util.List;

public class BooleanOptionParser implements OptionParser<Boolean> {

    @Override
    public Boolean parse(List<String> arguments, Option option) {
        int index = arguments.indexOf("-" + option.value());

        if (index == -1) {
            return false;
        }

        if (index + 1 < arguments.size() && !arguments.get(index + 1).startsWith("-")) {
            throw new TooManyArgumentException(option.value());
        }

        return true;
    }


}
