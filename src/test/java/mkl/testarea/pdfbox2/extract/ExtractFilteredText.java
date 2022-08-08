package mkl.testarea.pdfbox2.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.emory.mathcs.backport.java.util.Arrays;

public class ExtractFilteredText {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/63936154/how-to-identify-and-remove-hidden-text-from-the-pdf-using-pdfbox-java">
     * How to identify and remove hidden text from the PDF using PDFBox java
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1jFhF9y8jh_tr9POU258Fvn9WDRGdv-4-/view?usp=drivesdk">
     * ES1315248.pdf
     * </a>
     * <p>
     * This test uses the {@link PDFFilteringTextStripper} to extract
     * the text from ES1315248.pdf except the "DRAFT - UNAUDITED" drawn
     * in white (CMYK 0,0,0,0).
     * </p>
     */
    @Test
    public void testExtractNoWhiteTextES1315248() throws IOException {
        float[] colorToFilter = new float[] {0,0,0,0};
        try (   InputStream resource = getClass().getResourceAsStream("ES1315248.pdf");
                PDDocument document = PDDocument.load(resource)  ) {
            PDFFilteringTextStripper stripper = new PDFFilteringTextStripper((text, gs) -> {
                PDColor color = gs.getNonStrokingColor();
                return color == null || !((color.getColorSpace() instanceof PDDeviceCMYK) && Arrays.equals(color.getComponents(), colorToFilter));
            });
            stripper.setEndPage(6);
            String text = stripper.getText(document);
            Files.write(new File(RESULT_FOLDER, "ES1315248-page1_6-filtered.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/63936154/how-to-identify-and-remove-hidden-text-from-the-pdf-using-pdfbox-java">
     * How to identify and remove hidden text from the PDF using PDFBox java
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1bEcpJheSWTl29B1SGheSv34k9S6VIMeb/view?usp=drivesdk">
     * ES1214377.pdf
     * </a>
     * <p>
     * This test uses the {@link PDFFilteringTextStripper} to extract
     * the text from ES1214377.pdf except the "DRAFT - UNAUDITED" drawn
     * in 0.753 in a Gray Gamma 2.2 XYZ ICCBased colorspace.
     * </p>
     */
    @Test
    public void testExtractNoGrayTextES1214377() throws IOException {
        float[] colorToFilter = new float[] {0.753f};
        try (   InputStream resource = getClass().getResourceAsStream("ES1214377.pdf");
                PDDocument document = PDDocument.load(resource)  ) {
            PDFFilteringTextStripper stripper = new PDFFilteringTextStripper((text, gs) -> {
                PDColor color = gs.getNonStrokingColor();
                return color == null || !((color.getColorSpace() instanceof PDICCBased) && Arrays.equals(color.getComponents(), colorToFilter));
            });
            stripper.setEndPage(6);
            String text = stripper.getText(document);
            Files.write(new File(RESULT_FOLDER, "ES1214377-page1_6-filtered.txt").toPath(), Collections.singleton(text));
        }
    }
}
