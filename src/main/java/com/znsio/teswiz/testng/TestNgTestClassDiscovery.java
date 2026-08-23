package com.znsio.teswiz.testng;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class TestNgTestClassDiscovery {
    private TestNgTestClassDiscovery() { }

    public static List<String> discoverTestClassesIn(String packageName) {
        Reflections reflections = new Reflections(packageName, Scanners.MethodsAnnotated);
        Set<String> testClassNames = new TreeSet<>();
        reflections.getMethodsAnnotatedWith(Test.class)
                .forEach(method -> testClassNames.add(method.getDeclaringClass().getName()));
        return List.copyOf(testClassNames);
    }
}
