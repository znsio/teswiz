package com.znsio.teswiz.aspect;

import org.apache.logging.log4j.Level;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class ConsumerLayerAspectLogging {
    @Pointcut("execution(public * *.*.*.steps.*.*(..))" +
              "|| execution(public * *.*.*.businessLayer.*.*.*(..))" +
              "|| execution(public * *.*.*.screen.*.*.*.*(..))")
    public void executionScope() {
    }

    @Before("executionScope()")
    public void beforeAnyMethod(JoinPoint joinPoint) {
        AspectJMethodLoggers.beforeAnyMethod(joinPoint, Level.INFO);
    }

    @After("executionScope()")
    public void afterAnyMethod(JoinPoint joinPoint) {
        AspectJMethodLoggers.afterAnyMethod(joinPoint, Level.INFO);
    }

}
