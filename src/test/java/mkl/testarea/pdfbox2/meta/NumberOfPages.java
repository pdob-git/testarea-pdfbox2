package mkl.testarea.pdfbox2.meta;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;

/**
 * @author mkl
 */
public class NumberOfPages {
    /**
     * <a href="https://stackoverflow.com/questions/76544095/org-apache-pdfbox-pdmodel-pddocument-retrieve-only-1-page-from-my-pdf-file">
     * org.apache.pdfbox.pdmodel.PDDocument retrieve only 1 page from my PDF file
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/107NZDAXOALVmJoOFEHAuLMIgSWSS1CCd/view?usp=sharing">
     * EUserPDF105215.pdf
     * </a>
     * <p>
     * I could not reproduce the issue, in my test document.getNumberOfPages() returns 14, not 1.
     * </p>
     */
    @Test
    public void testEUserPDF105215() throws IOException {
        try (
            InputStream resource = getClass().getResourceAsStream("EUserPDF105215.pdf");
            PDDocument document = PDDocument.load(resource);
        ){
            assertEquals("Incorrect number of pages reported", 14, document.getNumberOfPages());
        }
    }
}
