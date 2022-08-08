package mkl.testarea.pdfbox2.meta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class CreatePortableCollection
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "meta");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/46642994/how-to-create-pdf-package-using-pdfbox">
     * How to create pdf package using PdfBox?
     * </a>
     * <p>
     * This test implements the equivalent of the OP's code turning
     * a regular PDF into a portable collection.
     * </p>
     */
    @Test
    public void test() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/pdfbox2/sign/test.pdf"))
        {
            PDDocument pdDocument = PDDocument.load(resource);

            COSDictionary collectionDictionary = new COSDictionary();
            collectionDictionary.setName(COSName.TYPE, "Collection");
            collectionDictionary.setName("View", "T");
            PDDocumentCatalog catalog = pdDocument.getDocumentCatalog();
            catalog.getCOSObject().setItem("Collection", collectionDictionary);

            pdDocument.save(new File(RESULT_FOLDER, "test-collection.pdf"));
        }
    }

}
