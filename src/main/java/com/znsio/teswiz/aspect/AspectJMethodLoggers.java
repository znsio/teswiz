package com.znsio.teswiz.aspect;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;

import java.lang.reflect.Array;
import java.util.stream.IntStream;

import static org.apache.logging.log4j.Level.*;

public class AspectJMethodLoggers {
    private static final Logger LOGGER = LogManager.getLogger(AspectJMethodLoggers.class.getName());

    private AspectJMethodLoggers() {
    }

    public static void beforeAnyMethod(JoinPoint joinPoint, Level level) {
        try {
            String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            Integer lineNumber = (joinPoint.getSourceLocation() != null) ? joinPoint.getSourceLocation().getLine() : -1;
            Object[] methodArgs = joinPoint.getArgs();

            String message = String.format("\t%n<<<%s%n>>>%n",
                                           generateBeforeMethodAspectJLogger(className, methodName, lineNumber, methodArgs));

            logAtLevel(level, message);
        } catch (Exception e) {
            LOGGER.warn("Failed to log before method: " + e.getMessage());
        }
    }

    private static void logAtLevel(Level level, String message) {
        if (level.equals(DEBUG)) {
            LOGGER.debug(message);
        } else if (level.equals(TRACE)) {
            LOGGER.trace(message);
        } else if (level.equals(WARN)) {
            LOGGER.warn(message);
        } else if (level.equals(ERROR)) {
            LOGGER.error(message);
        } else if (level.equals(FATAL)) {
            LOGGER.fatal(message);
        } else {
            LOGGER.info(message);  // Fallback to INFO for unhandled levels
        }
    }

    public static void afterAnyMethod(JoinPoint joinPoint, Level level) {
        try {
            String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
            String methodName = joinPoint.getSignature().getName();

            String message = String.format("\t%n<<<%s%n>>>%n", generateAfterMethodAspectJLogger(className, methodName));

            logAtLevel(level, message);
        } catch (Exception e) {
            LOGGER.warn("Failed to log after method: " + e.getMessage());
        }
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
            } else {
                loggerMessage.append(String.format("Type: '%s', Value: \"%s\"%n", argument.getClass().getSimpleName(), argument));
            }
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
