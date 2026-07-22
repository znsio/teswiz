package com.znsio.teswiz.screen;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ScreenContractSanityChecker {
    private static final String ROOT_PACKAGE = "com.znsio.teswiz.screen";
    private static final Set<String> IMPLEMENTATION_ROOTS = Set.of(
            "android",
            "ios",
            "web",
            "windows",
            "pdf");

    private final Path sourceRoot;

    public ScreenContractSanityChecker(Path sourceRoot) {
        this.sourceRoot = sourceRoot;
    }

    public static void main(String[] args) throws IOException {
        Path sourceRoot = args.length > 0
                ? Path.of(args[0])
                : Path.of("src/test/java/com/znsio/teswiz/screen");
        ValidationReport report = new ScreenContractSanityChecker(sourceRoot).validate();
        if (!report.isSuccessful()) {
            throw new NotImplementedException(report.toDisplayString());
        }
        System.out.println(report.toDisplayString());
    }

    ValidationReport validate() throws IOException {
        List<ContractValidationResult> results = contractClassNames().stream()
                .map(this::validateContract)
                .sorted(Comparator.comparing(ContractValidationResult::contractClassName))
                .toList();
        return new ValidationReport(results);
    }

    ContractValidationResult validateContract(String contractClassName) {
        Class<?> contractClass = loadClass(contractClassName);
        List<String> implementationClassNames = implementationClassNamesFor(contractClassName);
        List<String> violations = new ArrayList<>();

        if (implementationClassNames.isEmpty()) {
            violations.add("No screen implementations found");
        }

        for (String implementationClassName : implementationClassNames) {
            Class<?> implementationClass = loadClass(implementationClassName);
            violations.addAll(validateImplementation(contractClass, implementationClass).stream()
                    .map(message -> implementationClassName + ": " + message)
                    .toList());
        }

        return new ContractValidationResult(contractClassName, implementationClassNames, violations);
    }

    static List<String> validateImplementation(Class<?> contractClass, Class<?> implementationClass) {
        List<String> violations = new ArrayList<>();

        if (!contractClass.isAssignableFrom(implementationClass)) {
            violations.add("does not implement " + contractClass.getName());
            return violations;
        }

        if (Modifier.isAbstract(implementationClass.getModifiers())) {
            violations.add("is abstract");
        }

        try {
            implementationClass.getConstructor(Driver.class, Visual.class);
        } catch (NoSuchMethodException exception) {
            violations.add("missing public constructor (Driver, Visual)");
        }

        for (Method contractMethod : publicAbstractMethods(contractClass)) {
            try {
                Method implementationMethod = implementationClass.getMethod(contractMethod.getName(),
                        contractMethod.getParameterTypes());
                if (Modifier.isAbstract(implementationMethod.getModifiers())) {
                    violations.add("method remains abstract: " + methodSignature(contractMethod));
                }
            } catch (NoSuchMethodException exception) {
                violations.add("missing method: " + methodSignature(contractMethod));
            }
        }

        return violations;
    }

    private List<String> contractClassNames() throws IOException {
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith("Screen.java"))
                    .filter(path -> !isImplementationPath(path))
                    .map(this::toClassName)
                    .sorted()
                    .toList();
        }
    }

    private List<String> implementationClassNamesFor(String contractClassName) {
        String simpleName = contractClassName.substring(contractClassName.lastIndexOf('.') + 1);
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(simpleName))
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(this::isImplementationPath)
                    .map(this::toClassName)
                    .sorted()
                    .collect(Collectors.toCollection(LinkedHashSet::new))
                    .stream()
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to scan screen implementations", exception);
        }
    }

    private boolean isImplementationPath(Path path) {
        Path relativePath = sourceRoot.relativize(path);
        if (relativePath.getNameCount() == 0) {
            return false;
        }
        return IMPLEMENTATION_ROOTS.contains(relativePath.getName(0).toString());
    }

    private String toClassName(Path path) {
        Path relativePath = sourceRoot.relativize(path);
        String suffix = relativePath.toString().replace('/', '.').replace('\\', '.');
        suffix = suffix.substring(0, suffix.length() - ".java".length());
        return ROOT_PACKAGE + "." + suffix;
    }

    private static List<Method> publicAbstractMethods(Class<?> contractClass) {
        return Stream.of(contractClass.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .sorted(Comparator.comparing(ScreenContractSanityChecker::methodSignature))
                .toList();
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Unable to load class: " + className, exception);
        }
    }

    private static String methodSignature(Method method) {
        return method.getName() + "(" + Stream.of(method.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", ")) + ")";
    }

    record ContractValidationResult(String contractClassName, List<String> implementationClassNames,
                                    List<String> violations) {
        boolean isSuccessful() {
            return violations.isEmpty();
        }
    }

    static final class ValidationReport {
        private final List<ContractValidationResult> results;

        ValidationReport(List<ContractValidationResult> results) {
            this.results = List.copyOf(results);
        }

        boolean isSuccessful() {
            return results.stream().allMatch(ContractValidationResult::isSuccessful);
        }

        String toDisplayString() {
            String summary = String.format("Screen contract sanity check: %d contract(s), %d failure(s)",
                    results.size(),
                    results.stream().mapToInt(result -> result.violations().size()).sum());
            if (isSuccessful()) {
                return summary + System.lineSeparator() + "All discovered screen implementations are compliant.";
            }

            String failures = results.stream()
                    .filter(result -> !result.isSuccessful())
                    .map(result -> {
                        String implementations = result.implementationClassNames().isEmpty()
                                ? "none"
                                : String.join(", ", result.implementationClassNames());
                        String violations = result.violations().stream()
                                .map(violation -> "  - " + violation)
                                .collect(Collectors.joining(System.lineSeparator()));
                        return result.contractClassName() + System.lineSeparator()
                                + " Implementations: " + implementations + System.lineSeparator()
                                + violations;
                    })
                    .collect(Collectors.joining(System.lineSeparator() + System.lineSeparator()));
            return summary + System.lineSeparator() + failures;
        }
    }
}
