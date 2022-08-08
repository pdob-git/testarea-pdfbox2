package mkl.testarea.pdfbox2.extract;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.BeforeClass;
import org.junit.Test;

import mkl.testarea.pdfbox2.extract.PdfCheckBoxFinder.CheckBox;

public class ExtractCheckBoxes {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/64093610/extract-checkbox-value-out-of-pdf-1-7-using-pdfbox">
     * Extract Checkbox value out of PDF 1.7 using PDFBox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/12O9W3SE4l4EZg7WArYoL3e6M579I7w3U/view?usp=sharing">
     * Updated_Form.pdf
     * </a>
     * <p>
     * This test illustrates how to use the {@link PdfCheckBoxFinder}
     * to find check boxes and their check state.
     * </p>
     */
    @Test
    public void testExtractFromUpdatedForm() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("Updated_Form.pdf");
                PDDocument document = PDDocument.load(resource)  ) {
            for (PDPage page : document.getPages())
            {
                PdfCheckBoxFinder finder = new PdfCheckBoxFinder(page);
                finder.processPage(page);
                for (CheckBox checkBox : finder.getBoxes()) {
                    Point2D ll = checkBox.getLowerLeft();
                    Point2D ur = checkBox.getUpperRight();
                    String checked = checkBox.isChecked() ? "checked" : "not checked";
                    System.out.printf(Locale.ROOT, "* (%4.3f, %4.3f) - (%4.3f, %4.3f) - %s\n", ll.getX(), ll.getY(), ur.getX(), ur.getY(), checked);
                }
            }
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/64093610/extract-checkbox-value-out-of-pdf-1-7-using-pdfbox">
     * Extract Checkbox value out of PDF 1.7 using PDFBox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/12O9W3SE4l4EZg7WArYoL3e6M579I7w3U/view?usp=sharing">
     * Updated_Form.pdf
     * </a>
     * <p>
     * This test illustrates how to combine the {@link PdfCheckBoxFinder} results
     * with text extraction.
     * </p>
     */
    @Test
    public void testExtractInlinedInTextFromUpdatedForm() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("Updated_Form.pdf");
                PDDocument document = PDDocument.load(resource)  ) {
            PDType1Font font = PDType1Font.ZAPF_DINGBATS;
            for (PDPage page : document.getPages())
            {
                PdfCheckBoxFinder finder = new PdfCheckBoxFinder(page);
                finder.processPage(page);
                for (CheckBox checkBox : finder.getBoxes()) {
                    Point2D ll = checkBox.getLowerLeft();
                    Point2D ur = checkBox.getUpperRight();
                    String checkBoxString = checkBox.isChecked() ? "\u2714" : "\u2717";
                    try (   PDPageContentStream canvas = new PDPageContentStream(document, page, AppendMode.APPEND, false, true)) {
                        canvas.beginText();
                        canvas.setNonStrokingColor(1, 0, 0);
                        canvas.setFont(font, (float)(ur.getY()-ll.getY()));
                        canvas.newLineAtOffset((float)ll.getX(), (float)ll.getY());
                        canvas.showText(checkBoxString);
                        canvas.endText();
                    }
                }
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            Files.write(new File(RESULT_FOLDER,  "Updated_Form-withChecks.txt").toPath(), Collections.singleton(text));
            document.save(new File(RESULT_FOLDER, "Updated_Form-withChecks.pdf"));
        }
    }
}
