package com.znsio.teswiz.aspect;

import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.testng.TestNgCapturedStep;
import com.znsio.teswiz.testng.TestNgStepRecorder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

// Captures each consumer-authored (steps/businessLayer/screen) method call as a
// "step" for the TestNG-mode rich HTML report - only when running in TestNG mode,
// since Cucumber mode already has real Given/When/Then steps of its own.
// Excludes AspectJ's own generated $AjcClosureN nested classes: since this advice
// is @Around (unlike ConsumerLayerAspectLogging's @Before/@After), the compiler
// generates closure classes to combine multiple advices on the same join point,
// and those synthetic classes/methods would otherwise match the same pointcut.
@Aspect
public class TestNgStepCaptureAspect {
    @Pointcut("(execution(public * *.*.*.steps.*.*(..))"
              + "|| execution(public * *.*.*.businessLayer.*.*.*(..))"
              + "|| execution(public * *.*.*.screen.*.*.*.*(..)))"
              + "&& !within(*..*$AjcClosure*)")
    public void executionScope() { }

    @Around("executionScope()")
    public Object captureStep(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!Setup.isTestNgExecutionMode()) {
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String stepName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        String matchLocation = buildMatchLocation(signature);
        int stepIndex = TestNgStepRecorder.beginStep(stepName, matchLocation);
        long startNanos = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            TestNgStepRecorder.endStep(stepIndex, TestNgCapturedStep.PASSED, System.nanoTime() - startNanos);
            return result;
        } catch (Throwable t) {
            TestNgStepRecorder.endStep(stepIndex, TestNgCapturedStep.FAILED, System.nanoTime() - startNanos);
            throw t;
        }
    }

    private String buildMatchLocation(MethodSignature signature) {
        StringBuilder parameterTypes = new StringBuilder();
        Class<?>[] types = signature.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                parameterTypes.append(",");
            }
            parameterTypes.append(types[i].getName());
        }
        return signature.getDeclaringTypeName() + "." + signature.getName() + "(" + parameterTypes + ")";
    }
}
