package com.znsio.teswiz.aspect;

import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.testng.TestNgCapturedStep;
import com.znsio.teswiz.testng.TestNgStepRecorder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

// Captures each consumer-authored (steps/businessLayer/screen) method call as a
// "step" for the TestNG-mode rich HTML report - only when running in TestNG mode,
// since Cucumber mode already has real Given/When/Then steps of its own.
@Aspect
public class TestNgStepCaptureAspect {
    @Pointcut("execution(public * *.*.*.steps.*.*(..))"
              + "|| execution(public * *.*.*.businessLayer.*.*.*(..))"
              + "|| execution(public * *.*.*.screen.*.*.*.*(..))")
    public void executionScope() { }

    @Around("executionScope()")
    public Object captureStep(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!Setup.isTestNgExecutionMode()) {
            return joinPoint.proceed();
        }

        String stepName = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName();
        long startNanos = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            TestNgStepRecorder.recordStep(stepName, TestNgCapturedStep.PASSED, System.nanoTime() - startNanos);
            return result;
        } catch (Throwable t) {
            TestNgStepRecorder.recordStep(stepName, TestNgCapturedStep.FAILED, System.nanoTime() - startNanos);
            throw t;
        }
    }
}
