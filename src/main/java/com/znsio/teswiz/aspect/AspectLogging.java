package com.znsio.teswiz.aspect;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class AspectLogging {

    private static final Logger LOGGER = Logger.getLogger(AspectLogging.class.getName());
    private final TestExecutionContext context;

    public AspectLogging() {
        long threadId = Thread.currentThread().getId();
        LOGGER.info(String.format("AspectLogging: ThreadId: '%s': Constructor", threadId));
        context = SessionContext.getTestExecutionContext(threadId);
    }

//    pointcut publicMethodExecuted(): execution(public * *(..));
    @Pointcut(value = "execution (* com.znsio.teswiz.runner.*.*(..))")
    public void myPointCut(){
    }

    @Around("myPointCut()")
    public Object applicationLogger(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        LOGGER.info(String.format("Entering method: %s", proceedingJoinPoint.getSignature().getName()));

        Object[] arguments = proceedingJoinPoint.getArgs();
        for (Object argument : arguments) {
            if (argument != null) {
                LOGGER.info(String.format("With argument of type %s and value %s.", argument.getClass().toString(), argument));
            }
        }

        Object response = proceedingJoinPoint.proceed();
        if(response != null)
            LOGGER.info(String.format("Exit method: %s", response.toString()));
        return this;
    }

}
