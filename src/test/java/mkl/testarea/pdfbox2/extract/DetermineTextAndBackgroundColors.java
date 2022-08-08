package mkl.testarea.pdfbox2.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class DetermineTextAndBackgroundColors {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/54637141/background-color-is-incorrect-on-first-page-for-some-reason">
     * Background color is incorrect on first page for some reason
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1LXOuGmk67hQRwLXi5GBx-5n3-CJ_NV7h">
     * test.pdf
     * </a> as "test3DmitryK.pdf".
     * <p>
     * The underlying issue is a bug in {@link org.apache.pdfbox.pdmodel.common.function.PDFunctionType0#eval(float[])}
     * - it changes values in its parameter array which is something other PDFBox does not expect.
     * There is an additional error in the OP's code which is not relevant here, though.
     * </p>
     */
    @Test
    public void testPdfToTextInfoConverter() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("test3DmitryK.pdf");
                PDDocument pdDocument = PDDocument.load(resource)   ) {
            PdfToTextInfoConverter pdfToTextInfoConverter = new PdfToTextInfoConverter(pdDocument);
            pdfToTextInfoConverter.stripPage(0, 300);
        }
    }

}
