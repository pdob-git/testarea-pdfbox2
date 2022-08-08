package mkl.testarea.pdfbox2.analyze;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.Test;

/**
 * @author mkl
 */
public class TestClipPathFinder
{
    /**
     * <a href="http://stackoverflow.com/questions/28321374/how-to-get-page-content-height-using-pdfbox">
     * How to get page content height using pdfbox
     * </a>
     * <br/>
     * <a href="http://d.pr/f/137PF">
     * test-pdf4.pdf
     * </a>
     * <br/>
     * <a href="http://d.pr/f/15uBF">
     * test-pdf5.pdf
     * </a>
     * <p>
     * The clip paths found here correspond to the Illustrator compound elements.
     * </p>
     */
    @Test
    public void testTestPdf4() throws IOException
    {
        try (InputStream resource = getClass().getResourceAsStream("test-pdf4.pdf"))
        {
            System.out.println("test-pdf4.pdf");
            PDDocument document = PDDocument.load(resource);
            PDPage page = document.getPage(0);
            ClipPathFinder finder = new ClipPathFinder(page);
            finder.findClipPaths();
            
            for (Path path : finder)
            {
                System.out.println(path);
            }
            
            document.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/28321374/how-to-get-page-content-height-using-pdfbox">
     * How to get page content height using pdfbox
     * </a>
     * <br/>
     * <a href="http://d.pr/f/137PF">
     * test-pdf4.pdf
     * </a>
     * <br/>
     * <a href="http://d.pr/f/15uBF">
     * test-pdf5.pdf
     * </a>
     * <p>
     * The clip paths found here correspond to the Illustrator compound elements.
     * </p>
     */
    @Test
    public void testTestPdf5() throws IOException
    {
        try (InputStream resource = getClass().getResourceAsStream("test-pdf5.pdf"))
        {
            System.out.println("test-pdf5.pdf");
            PDDocument document = PDDocument.load(resource);
            PDPage page = document.getPage(0);
            ClipPathFinder finder = new ClipPathFinder(page);
            finder.findClipPaths();
            
            for (Path path : finder)
            {
                System.out.println(path);
            }
            
            document.close();
        }
    }
}
