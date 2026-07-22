package com.znsio.teswiz.screen;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public final class ScreenContractCoverageReporter {
    private final ScreenClassCatalog classCatalog;

    public ScreenContractCoverageReporter(Path sourceRoot) {
        this.classCatalog = new ScreenClassCatalog(sourceRoot);
    }

    public static void main(String[] args) throws IOException {
        Path sourceRoot = args.length > 0
                ? Path.of(args[0])
                : Path.of("src/test/java/com/znsio/teswiz/screen");
        CoverageReport report = new ScreenContractCoverageReporter(sourceRoot).buildReport();
        System.out.println(report.toDisplayString());
    }

    CoverageReport buildReport() throws IOException {
        List<ContractCoverage> contractCoverage = classCatalog.discoverContractClassNames().stream()
                .map(this::describeCoverage)
                .toList();
        return new CoverageReport(contractCoverage);
    }

    private ContractCoverage describeCoverage(String contractClassName) {
        List<String> implementationClassNames = classCatalog.discoverImplementationClassNames(contractClassName);
        List<TargetCoverage> targetCoverage = ScreenImplementationTarget.supportedTargets().stream()
                .map(target -> describeTargetCoverage(contractClassName, implementationClassNames, target))
                .toList();
        return new ContractCoverage(contractClassName, targetCoverage);
    }

    private TargetCoverage describeTargetCoverage(String contractClassName, List<String> implementationClassNames,
                                                  ScreenImplementationTarget target) {
        String expectedClassName = target.expectedClassName(contractClassName);
        boolean implemented = implementationClassNames.contains(expectedClassName);
        return new TargetCoverage(target.displayName(), expectedClassName, implemented);
    }

    record ContractCoverage(String contractClassName, List<TargetCoverage> targetCoverage) {
        boolean isComplete() {
            return targetCoverage.stream().allMatch(TargetCoverage::implemented);
        }

        List<TargetCoverage> missingTargets() {
            return targetCoverage.stream()
                    .filter(target -> !target.implemented())
                    .toList();
        }
    }

    record TargetCoverage(String targetName, String expectedClassName, boolean implemented) {
    }

    static final class CoverageReport {
        private final List<ContractCoverage> contractCoverage;

        CoverageReport(List<ContractCoverage> contractCoverage) {
            this.contractCoverage = List.copyOf(contractCoverage);
        }

        String toDisplayString() {
            return summaryLine()
                    + System.lineSeparator()
                    + missingCoverageBlock();
        }

        private String summaryLine() {
            long completeContracts = contractCoverage.stream().filter(ContractCoverage::isComplete).count();
            return String.format("Screen contract coverage: %d contract(s), %d fully covered, %d with gaps",
                    contractCoverage.size(),
                    completeContracts,
                    contractCoverage.size() - completeContracts);
        }

        private String missingCoverageBlock() {
            List<ContractCoverage> contractsWithGaps = contractCoverage.stream()
                    .filter(contract -> !contract.isComplete())
                    .toList();
            if (contractsWithGaps.isEmpty()) {
                return "All supported target combinations are implemented.";
            }

            return contractsWithGaps.stream()
                    .map(this::formatContractCoverage)
                    .collect(Collectors.joining(System.lineSeparator() + System.lineSeparator()));
        }

        private String formatContractCoverage(ContractCoverage contractCoverage) {
            String missingTargets = contractCoverage.missingTargets().stream()
                    .map(target -> "  - " + target.targetName() + " -> " + target.expectedClassName())
                    .collect(Collectors.joining(System.lineSeparator()));
            return contractCoverage.contractClassName() + System.lineSeparator() + missingTargets;
        }
    }
}
