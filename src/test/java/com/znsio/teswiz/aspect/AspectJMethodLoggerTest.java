package com.znsio.teswiz.aspect;

import com.znsio.teswiz.aspects.AspectJMethodLoggers;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AspectJMethodLoggerTest {

    private String methodName = "unitTestMethodName";
    private Object[] params;

    @Test
    void validateLoggerWithNoArguments() {
        params = new Object[]{};
        assertEquals("Entering method: unitTestMethodName with parameters:",
                AspectJMethodLoggers.generateBeforeMethodAspectJLogger(methodName, params));
    }

    @Test
    void validateLoggerWithNullArguments() {
        params = new Object[]{null};
        assertEquals("Entering method: unitTestMethodName with parameters:\n\tParam0: value \"null\"",
                AspectJMethodLoggers.generateBeforeMethodAspectJLogger(methodName, params));
    }

    @Test
    void validateLoggerWithStringArguments() {
        params = new Object[]{"StringParam"};
        assertEquals("Entering method: unitTestMethodName with parameters:\n\tParam0: type String : value \"StringParam\"",
                AspectJMethodLoggers.generateBeforeMethodAspectJLogger(methodName, params));
    }

    @Test
    void validateLoggerWithArrayArguments() {
        params = new Object[]{new int[]{1, 2, 3}};
        assertEquals("Entering method: unitTestMethodName with parameters:\n\tParam0: type int[] : value \"[1, 2, 3]\"",
                AspectJMethodLoggers.generateBeforeMethodAspectJLogger(methodName, params));
    }

    @Test
    void validateLoggerWithCollectionsArguments() {
        params = new Object[]{new ArrayList<String>() {{
            add("Array");
            add("List");
        }}, new HashMap<String, Integer>() {{
            put("Hash", 1);
            put("Map", 2);
        }}};
        assertEquals("Entering method: unitTestMethodName with parameters:\n\tParam0: type  : value \"[Array, List]\"\n" +
                        "\tParam1: type  : value \"{Hash=1, Map=2}\"",
                AspectJMethodLoggers.generateBeforeMethodAspectJLogger(methodName, params));
    }

    @Test
    void validateLoggerWithDifferentCombinationArguments() {
        params = new Object[]{null, "StringParam", 1, new String[]{"String", "Array"},
                new ArrayList<String>() {{
                    add("Array");
                    add("List");
                }}, new HashMap<String, Integer>() {{
            put("Hash", 1);
            put("Map", 2);
        }}};
        assertEquals("Entering method: unitTestMethodName with parameters:\n\tParam0: value \"null\"\n" +
                        "\tParam1: type String : value \"StringParam\"\n" +
                        "\tParam2: type Integer : value \"1\"\n" +
                        "\tParam3: type String[] : value \"[String, Array]\"\n" +
                        "\tParam4: type  : value \"[Array, List]\"\n" +
                        "\tParam5: type  : value \"{Hash=1, Map=2}\"",
                AspectJMethodLoggers.generateBeforeMethodAspectJLogger(methodName, params));
    }
    @Test
    void exitMethodLoggerValidation(){
        assertEquals("Exit method: unitTestMethodName",
                AspectJMethodLoggers.generateAfterMethodAspectJLogger(methodName));
    }
}
