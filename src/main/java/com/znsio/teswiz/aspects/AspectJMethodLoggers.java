package com.znsio.teswiz.aspects;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;

import java.lang.reflect.Array;

public class AspectJMethodLoggers {
    private static final Logger LOGGER = Logger.getLogger(AspectJMethodLoggers.class.getName());
    private final TestExecutionContext context;

    private static String enteringLogger;
    private static String exitLogger;

    public AspectJMethodLoggers() {
        long threadId = Thread.currentThread().getId();
        context = SessionContext.getTestExecutionContext(threadId);
    }

    public static void beforeAnyMethod(JoinPoint joinPoint) {
        LOGGER.info(String.format("\n%s",generateBeforeMethodAspectJLogger(joinPoint.getSignature().getName(), joinPoint.getArgs())));
    }

    public static void afterAnyMethod(JoinPoint joinPoint) {
        LOGGER.info(generateAfterMethodAspectJLogger(joinPoint.getSignature().getName()));
    }

    public static String generateBeforeMethodAspectJLogger(String methodName, Object[] arguments) {
        StringBuilder loggerMessage = new StringBuilder();
        loggerMessage.append(String.format("Entering method: %s with parameters:\n", methodName));

        int currentIndex = 0;
        for (Object argument : arguments) {
            if (argument == null)
                loggerMessage.append(String.format("\tParam%s: value \"null\"\n", currentIndex++));

            else if (argument.getClass().isArray()) {
                StringBuilder arrayMessage = new StringBuilder();
                arrayMessage.append("[");

                for (int arrayIndex = 0; arrayIndex < Array.getLength(argument); arrayIndex++) {
                    arrayMessage.append(String.format("%s, ", Array.get(argument, arrayIndex)));
                }

                arrayMessage.replace(arrayMessage.length() - 2, arrayMessage.length(), "]");
                loggerMessage.append(String.format("\tParam%s: type %s : value \"%s\"\n", currentIndex++,
                        argument.getClass().getSimpleName(), arrayMessage));

            } else
                loggerMessage.append(String.format("\tParam%s: type %s : value \"%s\"\n", currentIndex++,
                        argument.getClass().getSimpleName(), argument));
        }
        return loggerMessage.toString().trim();
    }

    public static String generateAfterMethodAspectJLogger(String methodName) {
        return String.format("Exit method: %s", methodName);
    }
}
