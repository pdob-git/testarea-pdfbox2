package mkl.testarea.pdfbox2.meta;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class SetCropBox
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "meta");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/39689004/pdfbox-2-0-3-set-cropbox-using-textposition-coordinates">
     * PDFBox 2.0.3 Set cropBox using TextPosition coordinates
     * </a>
     * <br/>
     * <a href="http://downloadcenter.samsung.com/content/UM/201504/20150407095631744/ENG-US_NMATSCJ-1.103-0330.pdf">
     * ENG-US_NMATSCJ-1.103-0330.pdf
     * </a>
     * <p>
     * This test shows how to set the crop box on page twelve.
     * </p>
     */
    @Test
    public void testSetCropBoxENG_US_NMATSCJ_1_103_0330() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("ENG-US_NMATSCJ-1.103-0330.pdf"))
        {
            PDDocument pdDocument = PDDocument.load(resource);
            PDPage page = pdDocument.getPage(12-1);
            page.setCropBox(new PDRectangle(40f, 680f, 510f, 100f));
            pdDocument.save(new File(RESULT_FOLDER, "ENG-US_NMATSCJ-1.103-0330-page12cropped.pdf"));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/39689004/pdfbox-2-0-3-set-cropbox-using-textposition-coordinates">
     * PDFBox 2.0.3 Set cropBox using TextPosition coordinates
     * </a>
     * <br/>
     * <a href="http://downloadcenter.samsung.com/content/UM/201504/20150407095631744/ENG-US_NMATSCJ-1.103-0330.pdf">
     * ENG-US_NMATSCJ-1.103-0330.pdf
     * </a>
     * <p>
     * This test shows how to set the crop box on page twelve and render the cropped page as image.
     * </p>
     */
    @Test
    public void testSetCropBoxImgENG_US_NMATSCJ_1_103_0330() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("ENG-US_NMATSCJ-1.103-0330.pdf"))
        {
            PDDocument pdDocument = PDDocument.load(resource);
            PDPage page = pdDocument.getPage(12-1);
            page.setCropBox(new PDRectangle(40f, 680f, 510f, 100f));

            PDFRenderer renderer = new PDFRenderer(pdDocument);
            BufferedImage img = renderer.renderImage(12 - 1, 4f);
            ImageIOUtil.writeImage(img, new File(RESULT_FOLDER, "ENG-US_NMATSCJ-1.103-0330-page12cropped.jpg").getAbsolutePath(), 300);
            pdDocument.close();
        }
    }
}
