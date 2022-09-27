package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class BackAndForthBetweenPages {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/73800664/is-is-possible-to-write-content-in-next-page-and-jump-back-to-previous-page-in-p">
     * Is is possible to write content in next page and jump back to previous page in PDF using PDFBox?
     * </a>
     * <p>
     * This test shows how to go back and forth between pages and add content.
     * </p>
     */
    @Test
    public void testForSagarKumar() throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDRectangle mediaBox = PDRectangle.LETTER;
            document.addPage(new PDPage(mediaBox));
            document.addPage(new PDPage(mediaBox));

            PDFont font = PDType1Font.HELVETICA;
            for (int column = 1; column < 6; column++) {
                try (PDPageContentStream canvas = new PDPageContentStream(document, document.getPage(0), AppendMode.APPEND, false)) {
                    canvas.beginText();
                    canvas.setFont(font, 12);
                    canvas.newLineAtOffset(column * (mediaBox.getWidth() / 7), mediaBox.getHeight() / 7);
                    canvas.showText("Column " + column + " A");
                    canvas.endText();
                }
                try (PDPageContentStream canvas = new PDPageContentStream(document, document.getPage(1), AppendMode.APPEND, false)) {
                    canvas.beginText();
                    canvas.setFont(font, 12);
                    canvas.newLineAtOffset(column * (mediaBox.getWidth() / 7), 6 * mediaBox.getHeight() / 7);
                    canvas.showText("Column " + column + " B");
                    canvas.endText();
                }
            }
            document.save(new File(RESULT_FOLDER, "BackAndForthBetweenPages.pdf"));
        }
    }
}
