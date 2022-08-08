// $Id$
package mkl.testarea.pdfbox2.merge;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class CopyPages
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "merge");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/37526904/page-is-cropped-in-new-document-in-pdfbox-while-copying">
     * Page is cropped in new document in PDFBox while copying
     * </a>
     * <br/>
     * AnnotationSample.Standard.pdf, <em>a sample output of PDF Clown</em>
     * 
     * <p>
     * This test uses the OP's code. Indeed, the result is incomplete.
     * </p>
     */
    @Test
    public void testWithAddPage() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("AnnotationSample.Standard.pdf")  )
        {
            PDDocument source = PDDocument.load(resource);
            PDDocument output = new PDDocument();
            PDPage page = source.getPages().get(0);
            output.addPage(page);
            output.save(new File(RESULT_FOLDER, "PageAddedFromAnnotationSample.Standard.pdf"));
            output.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/37526904/page-is-cropped-in-new-document-in-pdfbox-while-copying">
     * Page is cropped in new document in PDFBox while copying
     * </a>
     * <br/>
     * AnnotationSample.Standard.pdf, <em>a sample output of PDF Clown</em>
     * 
     * <p>
     * This test uses {@link PDDocument#importPage(PDPage)} instead of
     * {@link PDDocument#addPage(PDPage)}. The result still is incomplete.
     * </p>
     */
    @Test
    public void testWithImportPage() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("AnnotationSample.Standard.pdf")  )
        {
            PDDocument source = PDDocument.load(resource);
            PDDocument output = new PDDocument();
            PDPage page = source.getPages().get(0);
            output.importPage(page);
            output.save(new File(RESULT_FOLDER, "PageImportedFromAnnotationSample.Standard.pdf"));
            output.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/37526904/page-is-cropped-in-new-document-in-pdfbox-while-copying">
     * Page is cropped in new document in PDFBox while copying
     * </a>
     * <br/>
     * AnnotationSample.Standard.pdf, <em>a sample output of PDF Clown</em>
     * 
     * <p>
     * This test uses the {@link Splitter} helper class instead of either the OP's
     * {@link PDDocument#importPage(PDPage)} or {@link PDDocument#addPage(PDPage)}.
     * This result finally is complete.
     * </p>
     */
    @Test
    public void testWithSplitter() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("AnnotationSample.Standard.pdf")  )
        {
            PDDocument source = PDDocument.load(resource);

            Splitter splitter = new Splitter();
            List<PDDocument> results = splitter.split(source);
            Assert.assertEquals("Expected exactly one result document from splitting a single page document.", 1, results.size());
            PDDocument output = results.get(0);

            output.save(new File(RESULT_FOLDER, "PageSplitFromAnnotationSample.Standard.pdf"));
            output.close();
        }
    }
}
