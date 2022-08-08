package mkl.testarea.pdfbox2.form;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class RemoveXfa {
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-5012">
     * Signature Field Not Recognize
     * </a>
     * <br/>
     * <a href="https://issues.apache.org/jira/secure/attachment/13014820/PDF_orig_anon.pdf">
     * PDF_orig_anon.pdf
     * </a>
     * <p>
     * This code (essentially by Maruan Sahyoun) demonstrates how to
     * correctly remove XFA and UR3 from the OP's original example file.
     * </p>
     */
    @Test
    public void testRemoveXfaAndUr3FromPDF_orig_anon() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("PDF_orig_anon.pdf")  ) {
            PDDocument document = PDDocument.load(resource);

            PDDocumentCatalog documentCatalog = document.getDocumentCatalog();
            PDAcroForm acroForm = documentCatalog.getAcroForm();
            assertTrue(acroForm.hasXFA());
            assertFalse(acroForm.xfaIsDynamic());
            acroForm.getCOSObject().removeItem(COSName.XFA);
            assertFalse(acroForm.hasXFA());

            // Remove U3 usage signature
            COSDictionary permsDict = documentCatalog.getCOSObject().getCOSDictionary(COSName.PERMS);
            permsDict.removeItem(COSName.getPDFName("UR3"));

            document.save(new File(RESULT_FOLDER, "PDF_orig_anon-noXfaUr3.pdf"));
        }
    }

}
