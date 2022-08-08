// $Id$
package mkl.testarea.pdfbox2.analyze;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.Test;

/**
 * @author mkl
 */
public class TestGraphicsCounter
{
    /**
     * <a href="http://stackoverflow.com/questions/28321374/how-to-get-page-content-height-using-pdfbox">
     * How to get page content height using pdfbox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B65bQnJhC1mvbEVQQ0o0QU9STlU/view?usp=sharing">
     * test.pdf
     * </a>, here as <code>test-rivu.pdf</code>
     * <p>
     * Rivu's code from a comment to count lines etc.
     * </p>
     */
    @Test
    public void testCountTestLikeRivu() throws IOException
    {
        try (InputStream resource = getClass().getResourceAsStream("test-rivu.pdf"))
        {
            System.out.println("test-rivu.pdf");
            PDDocument document = PDDocument.load(resource);

            PDPage page = document.getPage(4);
            PDFStreamParser parser = new PDFStreamParser(page);
            parser.parse();
            List<Object> tokens = parser.getTokens();
            int lines=0;
            int curves=0;
            int rectangles=0;
            int doOps=0;
            int clipPaths=0;
            for (Object token:tokens){
                if (token instanceof Operator) {
                    Operator op=(Operator) token;
                    if ("do".equals(op.getName()))
                        doOps+=1;
                    else if ("W".equals(op.getName())|| "W*".equals(op.getName()))
                        clipPaths+=1;
                    else if ("l".equals(op.getName()) || "h".equals(op.getName()))
                        lines+=1;
                    else if ("c".equals(op.getName())||"y".equals(op.getName()) ||"v".equals(op.getName())){
                        System.out.println(op);
                        curves+=1;
                    }
                    else if ("re".equals(op.getName()))
                        rectangles+=1;


                }
            }
            System.out.println(lines + " lines, " + curves + " curves, " + rectangles + " rectangles, " + doOps + " xobjects, " + clipPaths + " clip paths");

            document.close();
        }
    }

}
