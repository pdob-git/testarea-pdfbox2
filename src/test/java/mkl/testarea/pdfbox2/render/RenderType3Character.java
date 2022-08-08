package mkl.testarea.pdfbox2.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType3CharProc;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class RenderType3Character
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "render");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/42032729/render-type3-font-character-as-image-using-pdfbox">
     * Render Type3 font character as image using PDFBox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B0f6X4SAMh2KRDJTbm4tb3E1a1U/view">
     * 4700198773.pdf
     * </a>
     * from
     * <a href="http://stackoverflow.com/questions/37754112/extract-text-with-custom-font-result-non-readble">
     * extract text with custom font result non readble
     * </a>
     * <p>
     * This test shows how one can render individual Type 3 font glyphs as bitmaps.
     * Unfortunately PDFBox out-of-the-box does not provide a class to render contents
     * of arbitrary XObjects, merely for rendering pages; thus, we simply create a page
     * with the glyph in question and render that page.   
     * </p>
     * <p>
     * As the OP did not provide a sample PDF, we simply use one from another
     * stackoverflow question. There obviously might remain issues with the
     * OP's files.
     * </p>
     */
    @Test
    public void testRender4700198773() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Method PDPageContentStreamWrite = PDPageContentStream.class.getSuperclass().getDeclaredMethod("write", String.class);
        PDPageContentStreamWrite.setAccessible(true);

        try (   InputStream resource = getClass().getResourceAsStream("4700198773.pdf"))
        {
            PDDocument document = PDDocument.load(resource);

            PDPage page = document.getPage(0);
            PDResources pageResources = page.getResources();
            COSName f1Name = COSName.getPDFName("F1");
            PDType3Font fontF1 = (PDType3Font) pageResources.getFont(f1Name);
            Map<String, Integer> f1NameToCode = fontF1.getEncoding().getNameToCodeMap();

            COSDictionary charProcsDictionary = fontF1.getCharProcs();
            for (COSName key : charProcsDictionary.keySet())
            {
                COSStream stream = (COSStream) charProcsDictionary.getDictionaryObject(key);
                PDType3CharProc charProc = new PDType3CharProc(fontF1, stream);
                PDRectangle bbox = charProc.getGlyphBBox();
                if (bbox == null)
                    bbox = charProc.getBBox();
                Integer code = f1NameToCode.get(key.getName());

                if (code != null)
                {
                    PDDocument charDocument = new PDDocument();
                    PDPage charPage = new PDPage(bbox);
                    charDocument.addPage(charPage);
                    charPage.setResources(pageResources);
                    PDPageContentStream charContentStream = new PDPageContentStream(charDocument, charPage);
                    charContentStream.beginText();
                    charContentStream.setFont(fontF1, bbox.getHeight());
//                    charContentStream.write(String.format("<%2X> Tj\n", code).getBytes());
                    PDPageContentStreamWrite.invoke(charContentStream, String.format("<%2X> Tj\n", code));
                    charContentStream.endText();
                    charContentStream.close();

                    File result = new File(RESULT_FOLDER, String.format("4700198773-%s-%s.png", key.getName(), code));
                    PDFRenderer renderer = new PDFRenderer(charDocument);
                    BufferedImage image = renderer.renderImageWithDPI(0, 96);
                    ImageIO.write(image, "PNG", result);
                    charDocument.save(new File(RESULT_FOLDER, String.format("4700198773-%s-%s.pdf", key.getName(), code)));
                    charDocument.close();
                }
            }
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/42032729/render-type3-font-character-as-image-using-pdfbox">
     * Render Type3 font character as image using PDFBox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B0f6X4SAMh2KRDJTbm4tb3E1a1U/view">
     * 4700198773.pdf
     * </a>
     * from
     * <a href="http://stackoverflow.com/questions/37754112/extract-text-with-custom-font-result-non-readble">
     * extract text with custom font result non readble
     * </a>
     * <p>
     * This test shows how one can render individual Type 3 font glyphs as bitmaps.
     * Unfortunately PDFBox out-of-the-box does not provide a class to render contents
     * of arbitrary XObjects, merely for rendering pages; thus, we simply create a page
     * with the glyph in question and render that page.   
     * </p>
     * <p>
     * As the OP did not provide a sample PDF, we simply use one from another
     * stackoverflow question. There obviously might remain issues with the
     * OP's files.
     * </p>
     */
    @Test
    public void testRenderSdnList() throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
    {
        Method PDPageContentStreamWrite = PDPageContentStream.class.getSuperclass().getDeclaredMethod("write", String.class);
        PDPageContentStreamWrite.setAccessible(true);

        try (   InputStream resource = getClass().getResourceAsStream("sdnlist.pdf"))
        {
            PDDocument document = PDDocument.load(resource);

            PDPage page = document.getPage(1);
            PDResources pageResources = page.getResources();
            COSName f1Name = COSName.getPDFName("R144");
            PDType3Font fontF1 = (PDType3Font) pageResources.getFont(f1Name);
            Map<String, Integer> f1NameToCode = fontF1.getEncoding().getNameToCodeMap();

            COSDictionary charProcsDictionary = fontF1.getCharProcs();
            for (COSName key : charProcsDictionary.keySet())
            {
                COSStream stream = (COSStream) charProcsDictionary.getDictionaryObject(key);
                PDType3CharProc charProc = new PDType3CharProc(fontF1, stream);
                PDRectangle bbox = charProc.getGlyphBBox();
                if (bbox == null)
                    bbox = charProc.getBBox();
                Integer code = f1NameToCode.get(key.getName());

                if (code != null)
                {
                    PDDocument charDocument = new PDDocument();
                    PDPage charPage = new PDPage(bbox);
                    charDocument.addPage(charPage);
                    charPage.setResources(pageResources);
                    PDPageContentStream charContentStream = new PDPageContentStream(charDocument, charPage);
                    charContentStream.beginText();
                    charContentStream.setFont(fontF1, bbox.getHeight());
                    //charContentStream.getOutputStream().write(String.format("<%2X> Tj\n", code).getBytes());
                    PDPageContentStreamWrite.invoke(charContentStream, String.format("<%2X> Tj\n", code));
                    charContentStream.endText();
                    charContentStream.close();

                    File result = new File(RESULT_FOLDER, String.format("sdnlist-%s-%s.png", key.getName(), code));
                    PDFRenderer renderer = new PDFRenderer(charDocument);
                    BufferedImage image = renderer.renderImageWithDPI(0, 96);
                    ImageIO.write(image, "PNG", result);
                    charDocument.save(new File(RESULT_FOLDER, String.format("sdnlist-%s-%s.pdf", key.getName(), code)));
                    charDocument.close();
                }
            }
        }
    }
}
