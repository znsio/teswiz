package com.znsio.teswiz.screen;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ScreenContractSanityChecker {
    private final ScreenClassCatalog classCatalog;
    private final PlaywrightTsScreenModuleSupport moduleSupport;
    private final boolean includeMissingTargets;

    public ScreenContractSanityChecker(Path sourceRoot) {
        this(sourceRoot, false);
    }

    public ScreenContractSanityChecker(Path sourceRoot, boolean includeMissingTargets) {
        this.classCatalog = new ScreenClassCatalog(sourceRoot);
        this.moduleSupport = new PlaywrightTsScreenModuleSupport();
        this.includeMissingTargets = includeMissingTargets;
    }

    public static void main(String[] args) throws IOException {
        Path sourceRoot = args.length > 0
                ? Path.of(args[0])
                : Path.of("src/test/java/com/znsio/teswiz/screen");
        boolean includeMissingTargets = containsFlag(args, "--include-missing-targets");
        ValidationReport report = new ScreenContractSanityChecker(sourceRoot, includeMissingTargets).validate();
        if (!report.isSuccessful()) {
            throw new NotImplementedException(report.toDisplayString());
        }
        System.out.println(report.toDisplayString());
    }

    private static boolean containsFlag(String[] args, String flag) {
        if (null == args) {
            return false;
        }
        for (String argument : args) {
            if (flag.equals(argument)) {
                return true;
            }
        }
        return false;
    }

    ValidationReport validate() throws IOException {
        List<ContractValidationResult> results = classCatalog.discoverContractClassNames().stream()
                .map(this::validateContract)
                .sorted(Comparator.comparing(ContractValidationResult::contractClassName))
                .toList();
        return new ValidationReport(results);
    }

    ContractValidationResult validateContract(String contractClassName) {
        Class<?> contractClass = ScreenContractReflection.loadClass(contractClassName);
        List<String> implementationClassNames = classCatalog.discoverImplementationClassNames(contractClassName);
        List<String> violations = new ArrayList<>();
        boolean hasPlaywrightTsModule = moduleSupport.hasModuleFor(contractClassName);

        if (implementationClassNames.isEmpty() && !hasPlaywrightTsModule) {
            violations.add("missing Java screen implementations");
            violations.add("missing playwright-ts module: src/test/resources/playwright/screens/"
                    + moduleSupport.expectedModulePathFor(contractClassName));
        }

        if (hasPlaywrightTsModule) {
            violations.addAll(validatePlaywrightTsModule(contractClass).stream()
                    .map(message -> "src/test/resources/playwright/screens/" + moduleSupport.modulePathFor(contractClassName) + ": "
                            + message)
                    .toList());
        }

        for (String implementationClassName : implementationClassNames) {
            Class<?> implementationClass = ScreenContractReflection.loadClass(implementationClassName);
            violations.addAll(validateImplementation(contractClass, implementationClass).stream()
                    .map(message -> implementationClassName + ": " + message)
                    .toList());
        }

        if (includeMissingTargets) {
            violations.addAll(findMissingTargetCoverage(contractClassName, implementationClassNames));
        }

        return new ContractValidationResult(contractClassName, implementationClassNames, violations);
    }

    private List<String> findMissingTargetCoverage(String contractClassName, List<String> implementationClassNames) {
        return ScreenImplementationTarget.supportedTargets().stream()
                .filter(target -> !isTargetImplemented(contractClassName, implementationClassNames, target))
                .map(target -> "missing target coverage: " + target.displayName() + " -> "
                        + target.expectedReference(contractClassName))
                .toList();
    }

    private boolean isTargetImplemented(String contractClassName, List<String> implementationClassNames,
            ScreenImplementationTarget target) {
        if (target == ScreenImplementationTarget.WEB_PLAYWRIGHT_TS) {
            return moduleSupport.hasModuleFor(contractClassName);
        }
        return implementationClassNames.contains(target.expectedReference(contractClassName));
    }

    private List<String> validatePlaywrightTsModule(Class<?> contractClass) {
        Set<String> exportedFunctions = moduleSupport.exportedFunctionsFor(contractClass.getName());
        return ScreenContractReflection.publicAbstractMethods(contractClass).stream()
                .filter(method -> !exportedFunctions.contains(method.getName()))
                .map(method -> "missing exported function: " + ScreenContractReflection.methodSignature(method))
                .toList();
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

        for (Method contractMethod : ScreenContractReflection.publicAbstractMethods(contractClass)) {
            try {
                Method implementationMethod = implementationClass.getMethod(contractMethod.getName(),
                        contractMethod.getParameterTypes());
                if (Modifier.isAbstract(implementationMethod.getModifiers())) {
                    violations.add("method remains abstract: " + ScreenContractReflection.methodSignature(contractMethod));
                }
            } catch (NoSuchMethodException exception) {
                violations.add("missing method: " + ScreenContractReflection.methodSignature(contractMethod));
            }
        }

        return violations;
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
                    .map(this::formatContractFailure)
                    .collect(Collectors.joining(System.lineSeparator() + System.lineSeparator()));
            return summary + System.lineSeparator() + failures;
        }

        private String formatContractFailure(ContractValidationResult result) {
            List<String> moduleViolations = result.violations().stream()
                    .filter(this::isPlaywrightTsModuleViolation)
                    .toList();
            List<String> targetCoverageViolations = result.violations().stream()
                    .filter(this::isTargetCoverageViolation)
                    .toList();
            List<String> javaViolations = result.violations().stream()
                    .filter(violation -> !isPlaywrightTsModuleViolation(violation) && !isTargetCoverageViolation(violation))
                    .toList();

            List<String> lines = new ArrayList<>();
            lines.add(result.contractClassName());
            lines.add(" Java implementations: " + formatImplementations(result.implementationClassNames()));
            if (!javaViolations.isEmpty()) {
                lines.add(" Java issues:");
                lines.add(formatViolations(javaViolations));
            }
            if (!moduleViolations.isEmpty()) {
                lines.add(" Playwright-TS module issues:");
                lines.add(formatViolations(moduleViolations));
            }
            if (!targetCoverageViolations.isEmpty()) {
                lines.add(" Target coverage issues:");
                lines.add(formatViolations(targetCoverageViolations));
            }
            return String.join(System.lineSeparator(), lines);
        }

        private String formatImplementations(List<String> implementationClassNames) {
            return implementationClassNames.isEmpty()
                    ? "none"
                    : String.join(", ", implementationClassNames);
        }

        private String formatViolations(List<String> violations) {
            return violations.stream()
                    .map(violation -> "  - " + violation)
                    .collect(Collectors.joining(System.lineSeparator()));
        }

        private boolean isPlaywrightTsModuleViolation(String violation) {
            return violation.startsWith("missing playwright-ts module:")
                    || violation.startsWith("src/test/resources/playwright/screens/");
        }

        private boolean isTargetCoverageViolation(String violation) {
            return violation.startsWith("missing target coverage:");
        }
    }
}
