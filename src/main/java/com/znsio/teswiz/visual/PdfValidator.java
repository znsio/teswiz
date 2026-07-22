package com.znsio.teswiz.visual;

import com.applitools.eyes.TestResults;
import com.znsio.teswiz.runner.Visual;

public class PdfValidator {
    private final PdfValidationExecutor executor;

    public PdfValidator(Visual visual) {
        this(new VisualPdfValidationExecutor(visual));
    }

    PdfValidator(PdfValidationExecutor executor) {
        this.executor = executor;
    }

    public TestResults validate() {
        return executor.validate();
    }

    public TestResults validate(int[] pageNumbers) {
        return executor.validate(pageNumbers);
    }

    public TestResults validate(String pdfFileName) {
        return executor.validate(pdfFileName);
    }

    public TestResults validate(String pdfFileName, int[] pageNumbers) {
        return executor.validate(pdfFileName, pageNumbers);
    }

    interface PdfValidationExecutor {
        TestResults validate();

        TestResults validate(int[] pageNumbers);

        TestResults validate(String pdfFileName);

        TestResults validate(String pdfFileName, int[] pageNumbers);
    }

    private record VisualPdfValidationExecutor(Visual visual) implements PdfValidationExecutor {
        @Override
        public TestResults validate() {
            return visual.validatePdf();
        }

        @Override
        public TestResults validate(int[] pageNumbers) {
            return visual.validatePdf(pageNumbers);
        }

        @Override
        public TestResults validate(String pdfFileName) {
            return visual.validatePdf(pdfFileName);
        }

        @Override
        public TestResults validate(String pdfFileName, int[] pageNumbers) {
            return visual.validatePdf(pdfFileName, pageNumbers);
        }
    }
}
