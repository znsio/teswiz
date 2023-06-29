package com.znsio.teswiz.aspect;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.aspects.AspectJMethodLoggers;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect
public class AspectLogging {
    private static final Logger LOGGER = Logger.getLogger(AspectLogging.class.getName());
    private final TestExecutionContext context;

    public AspectLogging() {
        long threadId = Thread.currentThread().getId();
        context = SessionContext.getTestExecutionContext(threadId);
    }
    @Pointcut("execution(public * *.*.*.businessLayer.*.*.*(..)) " +
            "|| execution(public * *.*.*.screen.*.*.*.*(..))" +
            "|| execution(public * *.*.*.entities.*.*(..))" +
            "|| execution(public * *.*.*.listener.*.*(..))" +
            "|| execution(public * *.*.*.runner.*.*(..))" +
            "|| execution(public * *.*.*.steps.*Steps.*(..))")
    public void executionScope(){
    }

    @Before("executionScope()")
    public void beforeAnyMethod(JoinPoint joinPoint) {
        AspectJMethodLoggers.beforeAnyMethod(joinPoint);
    }

    @After("executionScope()")
    public void afterAnyMethod(JoinPoint joinPoint) {
        AspectJMethodLoggers.afterAnyMethod(joinPoint);
    }

}
