package com.znsio.teswiz.runner;

/**
 * Exists solely so AspectLogging's weaving of the runner package is verifiable in a test:
 * AOP compile-time weaving is scoped per source set, so proving it fires requires a target
 * method that is itself compiled in src/main.
 */
public class AspectLoggingProbe {
    public String doSomething() {
        return "done";
    }
}
