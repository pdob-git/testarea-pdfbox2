package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class AddContentToOCG
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/49498074/pdfbox-how-can-i-add-content-to-a-layer">
     * PDFbox - how can i add content to a layer?
     * </a>
     * <p>
     * This test show how add content to an optional content group which
     * may or may not already exist.
     * </p>
     * <p>
     * This is the PDFBox 2 equivalent of the PDFBox 1 test with the same name
     * in response to
     * <a href="http://stackoverflow.com/questions/43275212/how-do-i-make-modifications-to-existing-layeroptional-content-group-in-pdf">
     * How do I make modifications to existing layer(Optional Content Group) in pdf?
     * </a>
     * </p>
     * @see #addTextToLayer(PDDocument, int, String, float, float, String)
     */
    @Test
    public void testAddContentToNewOrExistingOCG() throws IOException
    {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        addTextToLayer(document, 0, "MyLayer", 30, 600, "Text in new layer 'MyLayer'");
        addTextToLayer(document, 0, "MyOtherLayer", 230, 550, "Text in new layer 'MyOtherLayer'");
        addTextToLayer(document, 0, "MyLayer", 30, 500, "Text in existing layer 'MyLayer'");
        addTextToLayer(document, 0, "MyOtherLayer", 230, 450, "Text in existing layer 'MyOtherLayer'");

        document.save(new File(RESULT_FOLDER, "TextInOCGs.pdf"));
        document.close();
    }

    /**
     * @see #testAddContentToNewOrExistingOCG()
     */
    void addTextToLayer(PDDocument document, int pageNumber, String layerName, float x, float y, String text) throws IOException
    {
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        PDOptionalContentProperties ocprops = catalog.getOCProperties();
        if (ocprops == null)
        {
            ocprops = new PDOptionalContentProperties();
            catalog.setOCProperties(ocprops);
        }
        PDOptionalContentGroup layer = null;
        if (ocprops.hasGroup(layerName))
        {
            layer = ocprops.getGroup(layerName);
        }
        else
        {
            layer = new PDOptionalContentGroup(layerName);
            ocprops.addGroup(layer);
        }

        PDPage page = (PDPage) document.getPage(pageNumber);

        PDResources resources = page.getResources();
        if (resources == null)
        {
            resources = new PDResources();
            page.setResources(resources);
        }

        PDFont font = PDType1Font.HELVETICA;

        PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, true, true);
        contentStream.beginMarkedContent(COSName.OC, layer);
        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
        contentStream.endMarkedContent();
        
        contentStream.close();
    }
}
