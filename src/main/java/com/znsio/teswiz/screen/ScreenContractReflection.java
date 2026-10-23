package com.znsio.teswiz.screen;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class ScreenContractReflection {
    private ScreenContractReflection() {
    }

    static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Unable to load class: " + className, exception);
        }
    }

    static List<Method> publicAbstractMethods(Class<?> contractClass) {
        return Stream.of(contractClass.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .sorted(Comparator.comparing(ScreenContractReflection::methodSignature))
                .toList();
    }

    static String methodSignature(Method method) {
        return method.getName() + "(" + Stream.of(method.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", ")) + ")";
    }
}
