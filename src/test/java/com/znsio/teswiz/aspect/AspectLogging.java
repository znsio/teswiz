package com.znsio.teswiz.aspect;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect
public class AspectLogging {
    private static final Logger LOGGER = Logger.getLogger(AspectLogging.class.getName());
    private final TestExecutionContext context;

    public AspectLogging() {
        long threadId = Thread.currentThread().getId();
        LOGGER.info(String.format("AspectLogging: ThreadId: '%s': Constructor", threadId));
        context = SessionContext.getTestExecutionContext(threadId);
    }

    @Before("execution(public * com.znsio.teswiz.businessLayer.*.*.*(..))")
    public void beforeAnyMethod(JoinPoint joinPoint) {
        LOGGER.info(String.format("Entering method: %s", joinPoint.getSignature().getName()));
        Object[] arguments = joinPoint.getArgs();
        for (Object argument : arguments) {
            if (argument != null) {
                LOGGER.info(String.format("With argument of type %s and value %s.", argument.getClass().toString(), argument));
            }
        }
    }

    @After("execution(public * com.znsio.teswiz.businessLayer.*.*.*(..))")
    public void afterAnyMethod(JoinPoint joinPoint){
        LOGGER.info(String.format("Exit method: %s", joinPoint.getSignature().getName()));
    }

}
