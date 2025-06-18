package com.znsio.teswiz.aspect;

import com.znsio.teswiz.tools.FileUtils;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AspectJMethodLoggerTest {
    private static final String className = AspectJMethodLoggerTest.class.getSimpleName();
    private static final String LOG_DIR = "./target/testLogs";

    private Object[] params;

    @BeforeAll
    public static void setupBefore() {
        System.setProperty("LOG_DIR", LOG_DIR);
        FileUtils.createDirectory(LOG_DIR);
    }

    @Test
    void validateLoggerWithNoArguments() {
        StackTraceElement stackTraceElement = new Throwable().getStackTrace()[0];
        String methodName = stackTraceElement.getMethodName();
        int lineNumber = stackTraceElement.getLineNumber();
        String enteringMethodPrefix = String.format("Entering method: '%s.%s:%d'", className,
                methodName, lineNumber);
        params = new Object[]{};
        assertEquals(enteringMethodPrefix,
                AspectJMethodLoggers.generateBeforeMethodAspectJLogger(className, methodName, lineNumber,
                        params));
    }

    @Test
    void validateLoggerWithNullArguments() {
        StackTraceElement stackTraceElement = new Throwable().getStackTrace()[0];
        String methodName = stackTraceElement.getMethodName();
        int lineNumber = stackTraceElement.getLineNumber();
        String enteringMethodPrefix = String.format("Entering method: '%s.%s:%d'", className,
                methodName, lineNumber);
        params = new Object[]{null};
        assertEquals(String.format("%s%n\tParam-0: Value: \"null\"", enteringMethodPrefix),
                AspectJMethodLoggers.generateBeforeMethodAspectJLogger(className, methodName, lineNumber, params));
    }

    @Test
    void validateLoggerWithStringArguments() {
        StackTraceElement stackTraceElement = new Throwable().getStackTrace()[0];
        String methodName = stackTraceElement.getMethodName();
        int lineNumber = stackTraceElement.getLineNumber();
        String enteringMethodPrefix = String.format("Entering method: '%s.%s:%d'", className,
                methodName, lineNumber);
        params = new Object[]{"StringParam"};
        assertEquals(String.format("%s%n\tParam-0: Type: 'String', Value: \"StringParam\"", enteringMethodPrefix),
                AspectJMethodLoggers.generateBeforeMethodAspectJLogger(className, methodName,
                        lineNumber, params));
    }

    @Test
    void validateLoggerWithArrayArguments() {
        StackTraceElement stackTraceElement = new Throwable().getStackTrace()[0];
        String methodName = stackTraceElement.getMethodName();
        int lineNumber = stackTraceElement.getLineNumber();
        String enteringMethodPrefix = String.format("Entering method: '%s.%s:%d'", className,
                methodName, lineNumber);
        params = new Object[]{new int[]{1, 2, 3}};
        assertEquals(String.format("%s\n\tParam-0: Type: 'int[]', Value: \"[1, 2, 3]\"", enteringMethodPrefix),
                AspectJMethodLoggers.generateBeforeMethodAspectJLogger(className, methodName,
                        lineNumber, params));
    }

    @Test
    void validateLoggerWithCollectionsArguments() {
        StackTraceElement stackTraceElement = new Throwable().getStackTrace()[0];
        String methodName = stackTraceElement.getMethodName();
        int lineNumber = stackTraceElement.getLineNumber();
        String enteringMethodPrefix = String.format("Entering method: '%s.%s:%d'", className,
                methodName, lineNumber);
        params = new Object[]{new ArrayList<String>() {{
            add("Array");
            add("List");
        }}, new HashMap<String, Integer>() {{
            put("Hash", 1);
            put("Map", 2);
        }}};
        assertEquals(String.format("%s\n\tParam-0: Type: '', Value: \"[Array, List]\"\n\tParam-1: Type: '', Value: \"{Hash=1, Map=2}\"", enteringMethodPrefix),
                AspectJMethodLoggers.generateBeforeMethodAspectJLogger(className, methodName,
                        lineNumber, params));
    }

    @Test
    void validateLoggerWithDifferentCombinationArguments() {
        StackTraceElement stackTraceElement = new Throwable().getStackTrace()[0];
        String methodName = stackTraceElement.getMethodName();
        int lineNumber = stackTraceElement.getLineNumber();
        String enteringMethodPrefix = String.format("Entering method: '%s.%s:%d'", className,
                methodName, lineNumber);
        params = new Object[]{null, "StringParam", 1, new String[]{"String", "Array"},
                new ArrayList<String>() {{
                    add("Array");
                    add("List");
                }}, new HashMap<String, Integer>() {{
            put("Hash", 1);
            put("Map", 2);
        }}};
        assertEquals(String.format("%s\n\tParam-0: Value: \"null\"\n\tParam-1: Type: 'String', Value: \"StringParam\"\n\tParam-2: Type: 'Integer', Value: \"1\"\n\tParam-3: Type: 'String[]', Value: \"[String, Array]\"\n\tParam-4: Type: '', Value: \"[Array, List]\"\n\tParam-5: Type: '', Value: \"{Hash=1, Map=2}\"", enteringMethodPrefix),
                AspectJMethodLoggers.generateBeforeMethodAspectJLogger(className, methodName,
                        lineNumber, params));
    }

    @Test
    void exitMethodLoggerValidation() {
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        String exitMethodPrefix = String.format("Exiting method: '%s.%s'", className, methodName);
        assertEquals(exitMethodPrefix,
                AspectJMethodLoggers.generateAfterMethodAspectJLogger(className, methodName));
    }
}
