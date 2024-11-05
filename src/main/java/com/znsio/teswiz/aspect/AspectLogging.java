package com.znsio.teswiz.aspect;

import org.apache.logging.log4j.Level;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

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
        AspectJMethodLoggers.beforeAnyMethod(joinPoint, Level.DEBUG);
    }

    @After("executionScope()")
    public void afterAnyMethod(JoinPoint joinPoint) {
        AspectJMethodLoggers.afterAnyMethod(joinPoint, Level.DEBUG);
    }

}
