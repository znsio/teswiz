package com.znsio.teswiz.web.playwright;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class PlaywrightJavaScript {
    private PlaywrightJavaScript() {
    }

    static String adapt(String script) {
        return script.replace("arguments", "args");
    }

    static List<Object> adaptArguments(Object[] arguments) {
        List<Object> adaptedArguments = new ArrayList<>();
        Arrays.stream(arguments).forEach(argument -> adaptedArguments.add(adaptArgument(argument)));
        return adaptedArguments;
    }

    private static Object adaptArgument(Object argument) {
        if (argument instanceof PlaywrightJavaWebElement playwrightElement) {
            return playwrightElement.elementHandle();
        }
        return argument;
    }
}
