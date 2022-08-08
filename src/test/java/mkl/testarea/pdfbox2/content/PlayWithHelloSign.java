package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.BeforeClass;
import org.junit.Test;

import mkl.testarea.pdfbox2.content.HelloSignAnalyzer.HelloSignField;

/**
 * @author mkl
 */
public class PlayWithHelloSign
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/41071142/pdfbox-remove-a-single-field-from-pdf">
     * PDFBox: Remove a single field from PDF
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/oyv1vjyhkmao1t1/input.pdf?dl=0">
     * input.pdf
     * </a>
     * <p>
     * This method applies the {@link HelloSignAnalyzer} to the sample document
     * and outputs the found fields.
     * </p>
     */
    @Test
    public void testAnalyzeInput() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("input.pdf");
                PDDocument pdDocument = PDDocument.load(resource)   )
        {
            HelloSignAnalyzer helloSignAnalyzer = new HelloSignAnalyzer(pdDocument);

            Map<String, HelloSignField> fields = helloSignAnalyzer.analyze();

            System.out.printf("Found %s fields:\n\n", fields.size());

            for (Map.Entry<String, HelloSignField> entry : fields.entrySet())
            {
                System.out.printf("%s -> %s\n", entry.getKey(), entry.getValue());
            }

            System.out.printf("\nLast form name: %s\n", helloSignAnalyzer.getLastFormName());
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/41071142/pdfbox-remove-a-single-field-from-pdf">
     * PDFBox: Remove a single field from PDF
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/oyv1vjyhkmao1t1/input.pdf?dl=0">
     * input.pdf
     * </a>
     * <p>
     * This method applies the {@link HelloSignManipulator} to the sample document
     * and clears the field <code>var1001</code> (<i>address1</i>).
     * </p>
     */
    @Test
    public void testClearAddress1Input() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("input.pdf");
                PDDocument pdDocument = PDDocument.load(resource)   )
        {
            HelloSignAnalyzer helloSignAnalyzer = new HelloSignAnalyzer(pdDocument);

            HelloSignManipulator helloSignManipulator = new HelloSignManipulator(helloSignAnalyzer);

            helloSignManipulator.clearFields(Collections.singleton("var1001"));
            
            pdDocument.save(new File(RESULT_FOLDER, "input-clear-address1.pdf"));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/41071142/pdfbox-remove-a-single-field-from-pdf">
     * PDFBox: Remove a single field from PDF
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/oyv1vjyhkmao1t1/input.pdf?dl=0">
     * input.pdf
     * </a>
     * <p>
     * This method applies the {@link HelloSignManipulator} to the sample document
     * and clears the fields <code>var1004</code> (<i>zip</i>), <code>var1003</code>
     * (<i>state</i>), and <code>date2</code>.
     * </p>
     */
    @Test
    public void testClearZipStateDate2Input() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("input.pdf");
                PDDocument pdDocument = PDDocument.load(resource)   )
        {
            HelloSignAnalyzer helloSignAnalyzer = new HelloSignAnalyzer(pdDocument);

            HelloSignManipulator helloSignManipulator = new HelloSignManipulator(helloSignAnalyzer);

            helloSignManipulator.clearFields(Arrays.asList("var1004", "var1003", "date2"));
            
            pdDocument.save(new File(RESULT_FOLDER, "input-clear-zip-state-date2.pdf"));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/41071142/pdfbox-remove-a-single-field-from-pdf">
     * PDFBox: Remove a single field from PDF
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/oyv1vjyhkmao1t1/input.pdf?dl=0">
     * input.pdf
     * </a>
     * <p>
     * This method applies the {@link HelloSignManipulator} to the sample document
     * multiple times and clears the fields <code>var1004</code> (<i>zip</i>),
     * <code>var1003</code> (<i>state</i>), and <code>date2</code>, each field in a
     * separate call.
     * </p>
     */
    @Test
    public void testClearZipStateDate2SuccessivelyInput() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("input.pdf");
                PDDocument pdDocument = PDDocument.load(resource)   )
        {
            HelloSignAnalyzer helloSignAnalyzer = new HelloSignAnalyzer(pdDocument);

            HelloSignManipulator helloSignManipulator = new HelloSignManipulator(helloSignAnalyzer);

            helloSignManipulator.clearFields(Collections.singleton("var1004"));
            helloSignManipulator.clearFields(Collections.singleton("var1003"));
            helloSignManipulator.clearFields(Collections.singleton("date2"));
            
            pdDocument.save(new File(RESULT_FOLDER, "input-clear-successively-zip-state-date2.pdf"));
        }
    }
}
