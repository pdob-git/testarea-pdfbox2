package mkl.testarea.pdfbox2.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ExtractColorText {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/59031734/get-text-color-in-pdfbox">
     * Get text color in PDFBox
     * </a>
     * <p>
     * This test has already been executed for the original color text stripper class from my answer to
     * <a href="https://stackoverflow.com/questions/21430341/identifying-the-text-based-on-the-output-in-pdf-using-pdfbox">
     * Identifying the text based on the output in PDF using PDFBOX
     * </a>
     * </p>
     * 
     * @throws IOException
     */
    @Test
    public void testExtractFromFurzoSample() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("furzo Sample.pdf");
                PDDocument document = PDDocument.load(resource) ) {
            PDFTextStripper stripper = new ColorTextStripper();
            String text = stripper.getText(document);

            Files.write(new File(RESULT_FOLDER, "furzo Sample.txt").toPath(), text.getBytes("UTF-8"));

            System.out.println("/// furzo Sample.pdf ///");
            System.out.println("Stripped text with color:");
            System.out.println(">>>");
            System.out.println(text);
            System.out.println("<<<");
        }
    }
}
