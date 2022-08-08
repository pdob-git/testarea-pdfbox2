package mkl.testarea.pdfbox2.split;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mklink
 *
 */
public class TestCustomSplitter {
    final static File RESULT_FOLDER = new File("target/test-outputs", "split");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/58345483/how-to-separate-pdf-based-on-given-intervals">
     * How to separate pdf based on given intervals
     * </a>
     * <p>
     * Test the {@link CustomSplitter} using the OP's example values.
     * </p>
     */
    @Test
    public void testSplitForSaiKrishna() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/pdfbox2/analyze/test-rivu.pdf")) {
            PDDocument document = PDDocument.load(resource);
            Splitter splitter = new CustomSplitter(new int[] {2,6});

            List<PDDocument> documents = splitter.split(document);

            for (int i=0; i < documents.size(); i++) {
                documents.get(i).save(new File(RESULT_FOLDER, String.format("test-rivu-%d.pdf", i)));
            }
        }
    }

}
