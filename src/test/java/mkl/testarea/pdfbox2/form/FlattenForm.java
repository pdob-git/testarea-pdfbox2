package mkl.testarea.pdfbox2.form;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class FlattenForm {
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-4640">
     * PDF Annotations missed when merging documents
     * </a>
     * <br/>
     * <a href="https://issues.apache.org/jira/secure/attachment/12978955/highlighted%20pdf.pdf">
     * highlighted pdf.pdf
     * </a>
     * <p>
     * In a comment the OP reduced the issue to flattening a PDF
     * with annotations with transparency. Nonetheless, it cannot
     * be reproduced.
     * </p>
     */
    @Test
    public void testFlattenWithSideEffects() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("highlighted pdf.pdf")    ) {
            PDDocument pdDocument = PDDocument.load(resource);

            pdDocument.getDocumentCatalog().getAcroForm().flatten();

            pdDocument.save(new File(RESULT_FOLDER, "highlighted pdf-flattened.pdf"));
        }
    }

    /**
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-4889">
     * Cannot flatten this file.
     * </a>
     * <br/>
     * <a href="https://issues.apache.org/jira/secure/attachment/13005793/f1040sb%20test.pdf">
     * f1040sb test.pdf
     * </a>
     * <p>
     * Indeed, flattening stops with an exception. The cause is the
     * presence of annotations with a non-empty Rect and an empty
     * (0×0) BBox. An attempt to scale that empty BBox into the
     * non-empty Rect causes the exception.
     * </p>
     */
    @Test
    public void testFlattenF1040sbTest() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("f1040sb test.pdf")    ) {
            PDDocument pdDocument = PDDocument.load(resource);

            pdDocument.getDocumentCatalog().getAcroForm().flatten();

            pdDocument.save(new File(RESULT_FOLDER, "f1040sb test-flattened.pdf"));
        }
    }
}
