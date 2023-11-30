package com.znsio.teswiz.aspect;

import org.apache.log4j.*;
import org.aspectj.lang.*;

import java.lang.reflect.*;
import java.util.stream.*;

public class AspectJMethodLoggers {
    private static final Logger LOGGER = Logger.getLogger(AspectJMethodLoggers.class.getName());

    private AspectJMethodLoggers() {
    }

    public static void beforeAnyMethod(JoinPoint joinPoint) {
        LOGGER.info(String.format("%n\t<<<%s>>>",
                generateBeforeMethodAspectJLogger(joinPoint.getSignature().getDeclaringType().getSimpleName(),
                        joinPoint.getSignature().getName(),joinPoint.getSourceLocation().getLine(),
                joinPoint.getArgs())));
    }

    public static void afterAnyMethod(JoinPoint joinPoint) {
        LOGGER.info(String.format("%n\t<<<%s>>>",
                generateAfterMethodAspectJLogger(joinPoint.getSignature().getDeclaringType().getSimpleName(),
                        joinPoint.getSignature().getName())));
    }

    public static String generateBeforeMethodAspectJLogger(String simpleClassName,
                                                           String methodName,
                                                           int lineNumber, Object[] arguments) {
        StringBuilder loggerMessage = new StringBuilder();
        loggerMessage.append(String.format("Entering method: '%s.%s:%d'%n", simpleClassName, methodName, lineNumber));

        addParameterInfoToLoggerMessage(arguments, loggerMessage);
        return loggerMessage.toString().trim();
    }

    private static void addParameterInfoToLoggerMessage(Object[] arguments, StringBuilder loggerMessage) {
        for (int parameterIndex = 0; parameterIndex < arguments.length; parameterIndex++) {
            Object argument = arguments[parameterIndex];
            loggerMessage.append(String.format("\tParam-%s: ", parameterIndex));
            if (argument == null) {
                loggerMessage.append(String.format("Value: \"null\"%n"));
            } else if (argument.getClass().isArray()) {
                addArrayParameteInfoToLoggerMessage(loggerMessage, argument);
            } else
                loggerMessage.append(String.format("Type: '%s', Value: \"%s\"%n", argument.getClass().getSimpleName(), argument));
        }
    }

    private static void addArrayParameteInfoToLoggerMessage(StringBuilder loggerMessage,
                                                         Object argument) {
        StringBuilder arrayMessage = new StringBuilder();
        arrayMessage.append("[");
        IntStream.range(0, Array.getLength(argument)).mapToObj(arrayIndex -> String.format("%s, ", Array.get(argument, arrayIndex))).forEach(arrayMessage::append);
        arrayMessage.replace(arrayMessage.length() - 2, arrayMessage.length(), "]");
        loggerMessage.append(String.format("Type: '%s', Value: \"%s\"%n",
                argument.getClass().getSimpleName(), arrayMessage));
    }

    public static String generateAfterMethodAspectJLogger(String simpleClassName,
                                                          String methodName) {
        return String.format("Exiting method: '%s'", simpleClassName + "." + methodName);
    }
}
