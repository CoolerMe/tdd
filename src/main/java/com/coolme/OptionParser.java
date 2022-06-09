package com.coolme;

import java.util.List;

interface OptionParser {

  Object parse(List<String> arguments, String optionValue);
}
