package com.znsio.teswiz.visual;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

class PdfValidatorTest {
    @Test
    void shouldDelegateStandalonePdfValidation() {
        AtomicReference<String> invocation = new AtomicReference<>();
        PdfValidator pdfValidator = new PdfValidator(new FakeExecutor(invocation));

        pdfValidator.validate();

        assertThat(invocation.get()).isEqualTo("validate()");
    }

    @Test
    void shouldDelegateStandalonePdfValidationForSpecificPages() {
        AtomicReference<String> invocation = new AtomicReference<>();
        PdfValidator pdfValidator = new PdfValidator(new FakeExecutor(invocation));

        pdfValidator.validate(new int[]{1, 3});

        assertThat(invocation.get()).isEqualTo("validate([1, 3])");
    }

    @Test
    void shouldDelegateNamedPdfValidation() {
        AtomicReference<String> invocation = new AtomicReference<>();
        PdfValidator pdfValidator = new PdfValidator(new FakeExecutor(invocation));

        pdfValidator.validate("sample.pdf");

        assertThat(invocation.get()).isEqualTo("validate(sample.pdf)");
    }

    @Test
    void shouldDelegateNamedPdfValidationForSpecificPages() {
        AtomicReference<String> invocation = new AtomicReference<>();
        PdfValidator pdfValidator = new PdfValidator(new FakeExecutor(invocation));

        pdfValidator.validate("sample.pdf", new int[]{2, 4});

        assertThat(invocation.get()).isEqualTo("validate(sample.pdf, [2, 4])");
    }

    private static final class FakeExecutor implements PdfValidator.PdfValidationExecutor {
        private final AtomicReference<String> invocation;

        private FakeExecutor(AtomicReference<String> invocation) {
            this.invocation = invocation;
        }

        @Override
        public com.applitools.eyes.TestResults validate() {
            invocation.set("validate()");
            return null;
        }

        @Override
        public com.applitools.eyes.TestResults validate(int[] pageNumbers) {
            invocation.set("validate(" + java.util.Arrays.toString(pageNumbers) + ")");
            return null;
        }

        @Override
        public com.applitools.eyes.TestResults validate(String pdfFileName) {
            invocation.set("validate(" + pdfFileName + ")");
            return null;
        }

        @Override
        public com.applitools.eyes.TestResults validate(String pdfFileName, int[] pageNumbers) {
            invocation.set("validate(" + pdfFileName + ", " + java.util.Arrays.toString(pageNumbers) + ")");
            return null;
        }
    }
}
