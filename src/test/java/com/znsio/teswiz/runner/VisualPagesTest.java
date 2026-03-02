package com.znsio.teswiz.runner;

import com.znsio.teswiz.exceptions.InvalidTestDataException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VisualPagesTest {

    @Test
    void getPagesToProcessShouldReturnAllPagesWhenInputIsNull() {
        try (PDDocument document = createDocumentWithPages(3)) {
            int[] pages = Visual.getPagesToProcess(null, document);
            assertThat(pages).containsExactly(1, 2, 3);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getPagesToProcessShouldReturnProvidedPagesWhenValid() {
        try (PDDocument document = createDocumentWithPages(5)) {
            int[] pages = Visual.getPagesToProcess(new int[]{2, 4, 5}, document);
            assertThat(pages).containsExactly(2, 4, 5);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getPagesToProcessShouldThrowWhenAnyPageIsOutOfBounds() {
        try (PDDocument document = createDocumentWithPages(2)) {
            assertThrows(InvalidTestDataException.class,
                    () -> Visual.getPagesToProcess(new int[]{1, 3}, document));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static PDDocument createDocumentWithPages(int numberOfPages) {
        PDDocument document = new PDDocument();
        for (int index = 0; index < numberOfPages; index++) {
            document.addPage(new PDPage());
        }
        return document;
    }
}
