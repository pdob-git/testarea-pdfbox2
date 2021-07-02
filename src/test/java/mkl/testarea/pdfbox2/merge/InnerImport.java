package mkl.testarea.pdfbox2.merge;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class InnerImport {
    final static File RESULT_FOLDER = new File("target/test-outputs", "merge");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/68213927/pdfbox-small-pdf-file-cant-be-open-in-pdformxobject">
     * PDFBox : small pdf file can't be open in PDFormXObject
     * </a>
     * <br/>
     * <a href="https://wetransfer.com/downloads/583ee85f7d433f2b523fe27dea6df48520210701163459/b21644">
     * Pour Ronald - PDF Autocad reduit.pdf
     * </a>
     * <p>
     * Indeed, <code>importPageAsForm</code> first calls <code>importOcProperties</code>
     * which clones the source OCGs into the target OCGs using <code>PDFCloneUtility</code>.
     * As source and target are the same, something the utility does not expect, this results
     * in an endless loop adding the contents of an array to itself. A check for equality at
     * the top of <code>cloneMerge</code> can prevent this.
     * </p>
     */
    @Test
    public void testImportAsFormLikeGuyard() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("Pour Ronald - PDF Autocad reduit.pdf")) {
            PDDocument pdDocument = Loader.loadPDF(resource);
            PDFormXObject pageForm = new LayerUtility(pdDocument).importPageAsForm(pdDocument, 0);
            System.out.println(pageForm.getMatrix());
        }
    }
}
