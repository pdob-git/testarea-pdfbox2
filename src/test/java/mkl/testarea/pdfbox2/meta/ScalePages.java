package mkl.testarea.pdfbox2.meta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ScalePages {
    final static File RESULT_FOLDER = new File("target/test-outputs", "meta");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/49733329/java-stretch-pdf-pages-content">
     * Java- stretch pdf pages content
     * </a>
     * <p>
     * This test illustrates how to up-scale a PDF using the <b>UserUnit</b>
     * page property. 
     * </p>
     */
    @Test
    public void testUserUnitScaleAFieldTwice() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/pdfbox2/form/aFieldTwice.pdf")) {
            PDDocument document = PDDocument.load(resource);

            for (PDPage page : document.getPages()) {
                page.getCOSObject().setFloat("UserUnit", 1.7f);
            }

            document.save(new File(RESULT_FOLDER, "aFieldTwice-scaled.pdf"));
        }
    }

}
