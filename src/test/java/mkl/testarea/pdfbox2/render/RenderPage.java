// $Id$
package mkl.testarea.pdfbox2.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class RenderPage
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "render");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/37724865/pdfbox-2-0-1-hangs-rendering-pdf-page">
     * PDFBox 2.0.1 hangs rendering pdf page
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B5zMlyl8rHwsY3Y1WjFVZlllajA/view?usp=sharing">
     * 2E5D18CD314DC6B7E236C8546A2918.pdf
     * </a>
     * <p>
     * The issue can be reproduced in a Java 8 VM. As Tilman already mentioned in his answer,
     * it is an issue introduced by Java 8 using a different the color management system than
     * the former Java versions.
     * </p>
     * <p>
     * Analyzing the VM behavior with the new color management system it becomes clear that
     * the issue is not really a memory leak issue (as could be conjectured due to the excessive
     * memory use); instead objects are instantiated faster than garbage collection can collect
     * and free unused objects!
     * </p>
     * <p>
     * One can allow garbage collection to fetch up by changing the main loop of page content
     * parsing in PDFStreamEngine.processStreamOperators(PDContentStream):
     * </p>
     * <pre>
     * int i = 1;                         // new
     * while (token != null)
     * {
     *     if (token instanceof COSObject)
     *     {
     *         arguments.add(((COSObject) token).getObject());
     *     }
     *     else if (token instanceof Operator)
     *     {
     *         processOperator((Operator) token, arguments);
     *         arguments = new ArrayList<COSBase>();
     *     }
     *     else
     *     {
     *         arguments.add((COSBase) token);
     *     }
     *     token = parser.parseNextToken();
     *     if (i++ % 1000 == 0)           // new
     *         Runtime.getRuntime().gc(); // new
     * }
     * </pre>
     */
    @Test
    public void testRender2E5D18CD314DC6B7E236C8546A2918() throws IOException
    {
        File result = new File(RESULT_FOLDER, "2E5D18CD314DC6B7E236C8546A2918.png");
        try (   InputStream resource = getClass().getResourceAsStream("2E5D18CD314DC6B7E236C8546A2918.pdf"))
        {
            PDDocument document = PDDocument.load(resource);

            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, 96); //Gets stuck here
            ImageIO.write(image, "PNG", result);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/45831641/read-pdf-written-in-chinese-using-java">
     * read pdf written in chinese using java
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B6k7AYGPEth2djFMNVJ0dC1wLVU/view?usp=sharing">
     * sample1.pdf
     * </a>
     * <p>
     * Cannot reproduce the problem with the file at hand without concrete
     * code.
     * </p>
     */
    @Test
    public void testRenderSample1() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("sample1.pdf"))
        {
            PDDocument document = PDDocument.load(resource);

            PDFRenderer renderer = new PDFRenderer(document);

            for (int page = 0; page < document.getNumberOfPages(); page++)
            {
                BufferedImage image = renderer.renderImageWithDPI(page, 96);

                File result = new File(RESULT_FOLDER, String.format("sample1-%s.png", page));
                ImageIO.write(image, "PNG", result);
            }
        }
    }
}
