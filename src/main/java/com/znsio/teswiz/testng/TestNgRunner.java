package com.znsio.teswiz.testng;

import org.testng.TestNG;
import org.testng.xml.XmlSuite;

import java.util.List;

public final class TestNgRunner {
    private TestNgRunner() { }

    public static boolean run(List<String> testClassNames, TestNgGroupSelection groupSelection, int threadCount) {
        TestNG testNg = new TestNG();
        testNg.setTestClasses(resolveTestClasses(testClassNames));
        testNg.setParallel(XmlSuite.ParallelMode.METHODS);
        testNg.setThreadCount(threadCount);
        testNg.setUseDefaultListeners(false);
        testNg.addListener(new TeswizTestNgListener());
        if (!groupSelection.includedGroups().isEmpty()) {
            testNg.setGroups(String.join(",", groupSelection.includedGroups()));
        }
        if (!groupSelection.excludedGroups().isEmpty()) {
            testNg.setExcludedGroups(String.join(",", groupSelection.excludedGroups()));
        }

        testNg.run();

        return !testNg.hasFailure();
    }

    private static Class<?>[] resolveTestClasses(List<String> testClassNames) {
        return testClassNames.stream()
                .map(TestNgRunner::loadClass)
                .toArray(Class<?>[]::new);
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("TestNG test class not found: " + className, e);
        }
    }
}
