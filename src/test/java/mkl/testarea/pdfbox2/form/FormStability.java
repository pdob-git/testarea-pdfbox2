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
public class FormStability {
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/55045024/checked-checkbox-becomes-unchecked-on-save">
     * checked checkbox becomes unchecked on save
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1UnoguyJn215D5l7tQ2MNqlLN1SBPsgT-">
     * a.pdf
     * </a>
     * <p>
     * Cannot reproduce the issue.
     * </p>
     */
    @Test
    public void testLoadAndSaveAByBee() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("a.pdf")) {
            PDDocument doc = PDDocument.load(resource);
            doc.save(new File(RESULT_FOLDER, "a-resaved.pdf"));
        }
    }

}
