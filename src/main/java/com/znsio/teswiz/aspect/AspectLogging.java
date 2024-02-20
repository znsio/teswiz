package com.znsio.teswiz.aspect;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect
public class AspectLogging {
    @Pointcut("execution(public * *.*.*.entities.*.*(..)) " +
            "|| execution(public * *.*.*.listener.*.*(..)) " +
            "|| execution(public * *.*.*.runner.*.*(..)) " +
            "|| execution(public * *.*.*.steps.*.*(..)) " +
            "|| execution(public * *.*.*.tools.*.*.*(..))")
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
