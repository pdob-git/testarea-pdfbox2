package mkl.testarea.pdfbox2.meta;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author mkl
 */
public class DecryptRC4 {
    /**
     * <a href="https://stackoverflow.com/questions/54453983/pdfbox2-x-read-wrong-data-from-acroform">
     * PDFBOX2.X - Read wrong Data from acroForm
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=13NkvML3RHbU-Ms2x_eHAz01NdzadmZC1">
     * 2AF-0088-NA-LMF35-40-01-2019-01-18-14-25-52(1).pdf
     * </a>
     * <p>
     * Indeed, PDFBox before commit 2888ec6b19c7ea30535eb85db9d9ce6c04dc947c
     * (2019-02-02 12:40:52 by Tilman Hausherr) returned a wrong value here.
     * </p>
     */
    @Test
    public void test2AF_0088_NA_LMF35_40_01_2019_01_18_14_25_52_1_() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("2AF-0088-NA-LMF35-40-01-2019-01-18-14-25-52(1).pdf") ) {
            PDDocument pdDocument = PDDocument.load(resource);
            PDAcroForm pdAcroForm = pdDocument.getDocumentCatalog().getAcroForm();
            String value = pdAcroForm.getField("totalBuyoffCount").getValueAsString();
            Assert.assertEquals("The value of the field totalBuyoffCount is incorrect,", "3", value);
        }
    }

}
