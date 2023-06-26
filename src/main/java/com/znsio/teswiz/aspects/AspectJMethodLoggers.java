package com.znsio.teswiz.aspects;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;

public class AspectJMethodLoggers {
    private static final Logger LOGGER = Logger.getLogger(AspectJMethodLoggers.class.getName());
    private final TestExecutionContext context;

    public AspectJMethodLoggers() {
        long threadId = Thread.currentThread().getId();
        context = SessionContext.getTestExecutionContext(threadId);
    }

    public void beforeAnyMethod(JoinPoint joinPoint) {
        LOGGER.info(String.format("Entering method: %s ", joinPoint.getSignature().getName()));

        StringBuilder loggerMessage = new StringBuilder();
        Object[] arguments = joinPoint.getArgs();

        if (arguments.length != 0) {
            loggerMessage.append(String.format("With arguments- ", joinPoint.getSignature().getName()));

            for (Object argument : arguments) {
                if (argument != null) {
                    loggerMessage.append(String.format("type %s : value \"%s\", ", argument.getClass().getSimpleName(), argument));
                }
            }
            LOGGER.info(loggerMessage);
        }
    }

    public void afterAnyMethod(JoinPoint joinPoint) {
        LOGGER.info(String.format("Exit method: %s", joinPoint.getSignature().getName()));
    }
}
